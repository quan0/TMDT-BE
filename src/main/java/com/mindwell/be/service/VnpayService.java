package com.mindwell.be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindwell.be.config.VnpayConfig;
import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.VnpayTransaction;
import com.mindwell.be.repository.VnpayTransactionRepository;
import com.mindwell.be.util.VnpaySigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnpayService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNP_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayConfig vnpayConfig;
    private final VnpayTransactionRepository vnpayTransactionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.base-url:http://localhost:8000}")
    private String baseUrl;

    @Transactional
    public String createPaymentUrl(Payment payment, String clientIp) {
        if (payment == null || payment.getPaymentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment");
        }

        BigDecimal amountVnd = payment.getAmount();
        if (amountVnd == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount is required for VNPAY");
        }

        long vnd = amountVnd.setScale(0, RoundingMode.HALF_UP).longValue();
        if (vnd <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be > 0");
        }

        // Keep vnp_TxnRef stable and resolvable: we use paymentId like the VNPAY sample uses an orderId.
        String txnRef = String.valueOf(payment.getPaymentId());

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        String createDate = now.format(VNP_TIME);
        String expireDate = now.plusMinutes(15).format(VNP_TIME);

        String returnUrl = baseUrl + "/api/v1/payments/vnpay/return";

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnpayConfig.getApiVersion());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        // VNPAY requires order info to be Vietnamese without special characters.
        params.put("vnp_OrderInfo", "Thanh toan cho ma GD:" + txnRef);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", vnpayConfig.getLocale());
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", (clientIp == null || clientIp.isBlank()) ? "127.0.0.1" : clientIp);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        VnpaySigner.SignedQuery signed = VnpaySigner.sign(params, vnpayConfig.getHashSecret());

        String fullUrl = vnpayConfig.getPayUrl()
                + "?" + signed.queryString()
                + "&vnp_SecureHash=" + signed.secureHash();

        VnpayTransaction tx = vnpayTransactionRepository.findByPaymentPaymentId(payment.getPaymentId())
                .orElse(VnpayTransaction.builder().payment(payment).build());

        tx.setVnpTxnRef(txnRef);
        tx.setRequestHashData(signed.hashData());
        tx.setRequestSecureHash(signed.secureHash());
        tx.setRequestUrl(fullUrl);
        tx.setRequestParamsJson(toJson(params));

        vnpayTransactionRepository.save(tx);

        return fullUrl;
    }

    private String toJson(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
