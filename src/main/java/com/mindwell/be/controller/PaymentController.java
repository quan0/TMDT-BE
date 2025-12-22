package com.mindwell.be.controller;

import com.mindwell.be.entity.Payment;
import com.mindwell.be.entity.enums.PaymentStatus;
import com.mindwell.be.entity.enums.PaymentType;
import com.mindwell.be.dto.payment.MockPaymentSuccessDto;
import com.mindwell.be.dto.payment.PaymentListItemDto;
import com.mindwell.be.dto.common.PageResponse;
import com.mindwell.be.repository.PaymentRepository;
import com.mindwell.be.repository.VnpayTransactionRepository;
import com.mindwell.be.entity.VnpayTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.mindwell.be.util.VnpaySigner;
import com.mindwell.be.config.VnpayConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import com.mindwell.be.service.PaymentProcessingService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    private final PaymentProcessingService paymentProcessingService;
    private final VnpayTransactionRepository vnpayTransactionRepository;
    private final VnpayConfig vnpayConfig;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(
            summary = "List my payments",
            description = "Returns the authenticated user's payments (paged).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @Transactional(readOnly = true)
    public PageResponse<PaymentListItemDto> listMyPayments(
            @AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        int safeSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by(Sort.Order.desc("paymentId")));

        Page<Payment> payments = paymentRepository.searchMyPayments(principal.getUser().getUserId(), type, status, pageable);
        Page<PaymentListItemDto> dtoPage = payments.map(p -> new PaymentListItemDto(
                p.getPaymentId(),
                p.getPaymentType() == null ? null : p.getPaymentType().toJson(),
                p.getStatus() == null ? null : p.getStatus().toJson(),
                p.getAmount(),
                p.getRelatedId(),
                p.getMethod() == null ? null : p.getMethod().getMethodKey(),
                p.getMethod() == null ? null : p.getMethod().getDisplayName()
        ));

        return PageResponse.from(dtoPage);
    }

    @GetMapping("/{paymentId}/mock/redirect")
    @Operation(
        summary = "Mock payment provider redirect",
        description = "Development-only: simulates an external payment provider by marking the payment as PAID and returning a success JSON payload."
    )
    @Transactional
    public MockPaymentSuccessDto mockProviderRedirect(@PathVariable Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return paymentProcessingService.markPaidAndFinalize(payment);
    }

    @GetMapping("/vnpay/return")
    @Operation(
            summary = "VNPAY ReturnUrl",
            description = "Public callback for browser redirect after VNPAY payment. Accepts VNPAY query params (vnp_*), verifies checksum, stores payload, and updates payment status (idempotent)."
    )
    @Transactional
        public Map<String, Object> vnpayReturn(
            @RequestParam(required = false) Map<String, String> requestParams,
            HttpServletRequest request
        ) {
        Map<String, String> params = mergeParams(requestParams, request);
        String secureHash = params.get("vnp_SecureHash");

        boolean verified = VnpaySigner.verify(params, secureHash, vnpayConfig.getHashSecret());

        Payment payment = resolvePaymentFromVnpayParams(params);
        if (payment == null || payment.getPaymentId() == null) {
            String qs = request == null ? null : request.getQueryString();
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing/invalid vnp_TxnRef (or unknown order). Query=" + (qs == null ? "" : qs)
            );
        }

        Integer paymentId = payment.getPaymentId();

        upsertReturn(payment, params, request.getQueryString(), secureHash, verified);

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        boolean success = "00".equals(responseCode) && (transactionStatus == null || "00".equals(transactionStatus));

        if (verified && success) {
            paymentProcessingService.markPaidAndFinalize(payment);
        } else if (verified && responseCode != null && !"00".equals(responseCode)) {
            paymentProcessingService.markFailedIfPending(payment);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("paymentId", paymentId);
        res.put("verified", verified);
        res.put("vnp_ResponseCode", responseCode);
        res.put("vnp_TransactionStatus", transactionStatus);
        res.put("paymentStatus", payment.getStatus() == null ? null : payment.getStatus().toJson());
        return res;
    }

    @GetMapping("/vnpay/ipn")
    @Operation(
            summary = "VNPAY IPN",
            description = "Public server-to-server callback (IPN). Accepts VNPAY query params (vnp_*), verifies checksum, stores payload, and confirms payment. Returns {RspCode, Message}."
    )
    @Transactional
    public Map<String, String> vnpayIpn(
            @RequestParam(required = false) Map<String, String> requestParams,
            HttpServletRequest request
    ) {
        Map<String, String> params = mergeParams(requestParams, request);
        String secureHash = params.get("vnp_SecureHash");

        Payment payment = resolvePaymentFromVnpayParams(params);
        if (payment == null) {
            return Map.of("RspCode", "01", "Message", "Order not found");
        }

        Integer paymentId = payment.getPaymentId();

        boolean verified = VnpaySigner.verify(params, secureHash, vnpayConfig.getHashSecret());
        upsertIpn(payment, params, request.getQueryString(), secureHash, verified);

        if (!verified) {
            return Map.of("RspCode", "97", "Message", "Invalid signature");
        }

        // Amount validation (vnp_Amount is in VND * 100)
        String amountStr = params.get("vnp_Amount");
        if (amountStr != null && payment.getAmount() != null) {
            try {
                long vnpAmount = Long.parseLong(amountStr);
                long expected = payment.getAmount().longValue() * 100;
                if (vnpAmount != expected) {
                    return Map.of("RspCode", "04", "Message", "Invalid amount");
                }
            } catch (NumberFormatException ignored) {
                return Map.of("RspCode", "04", "Message", "Invalid amount");
            }
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return Map.of("RspCode", "02", "Message", "Order already confirmed");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);

        if (success) {
            paymentProcessingService.markPaidAndFinalize(payment);
            return Map.of("RspCode", "00", "Message", "Confirm Success");
        }

        paymentProcessingService.markFailedIfPending(payment);
        return Map.of("RspCode", "00", "Message", "Confirm Success");
    }

    private Map<String, String> readParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        if (request == null) return params;
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) params.put(k, v[0]);
            else params.put(k, null);
        });
        return params;
    }

    private Map<String, String> mergeParams(Map<String, String> requestParams, HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        if (requestParams != null && !requestParams.isEmpty()) {
            params.putAll(requestParams);
        }
        if (params.isEmpty()) {
            params.putAll(readParams(request));
        }
        return params;
    }

    private Integer parsePaymentId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) return null;
        try {
            return Integer.valueOf(txnRef.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Payment resolvePaymentFromVnpayParams(Map<String, String> params) {
        if (params == null) return null;

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            // Fallback: try to extract from vnp_OrderInfo like "Payment 123"
            String orderInfo = params.get("vnp_OrderInfo");
            txnRef = extractFirstNumber(orderInfo);
        }

        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }

        Integer numericPaymentId = parsePaymentId(txnRef);
        if (numericPaymentId != null) {
            return paymentRepository.findById(numericPaymentId).orElse(null);
        }

        // If merchant uses non-numeric vnp_TxnRef, resolve via stored transaction mapping.
        return vnpayTransactionRepository.findByVnpTxnRef(txnRef)
                .map(VnpayTransaction::getPayment)
                .orElse(null);
    }

    private String extractFirstNumber(String text) {
        if (text == null || text.isBlank()) return null;
        StringBuilder digits = new StringBuilder();
        boolean inNumber = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
                inNumber = true;
            } else if (inNumber) {
                break;
            }
        }
        return digits.isEmpty() ? null : digits.toString();
    }

    private void upsertReturn(Payment payment, Map<String, String> params, String query, String secureHash, boolean verified) {
        VnpayTransaction tx = vnpayTransactionRepository.findByPaymentPaymentId(payment.getPaymentId())
                .orElse(VnpayTransaction.builder().payment(payment).build());

        tx.setVnpTxnRef(params.get("vnp_TxnRef"));
        tx.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        tx.setVnpResponseCode(params.get("vnp_ResponseCode"));
        tx.setVnpTransactionStatus(params.get("vnp_TransactionStatus"));

        tx.setReturnQuery(query);
        tx.setReturnSecureHash(secureHash);
        tx.setReturnVerified(verified);
        tx.setReturnReceivedAt(LocalDateTime.now());
        tx.setReturnParamsJson(writeJson(params));

        vnpayTransactionRepository.save(tx);
    }

    private void upsertIpn(Payment payment, Map<String, String> params, String query, String secureHash, boolean verified) {
        VnpayTransaction tx = vnpayTransactionRepository.findByPaymentPaymentId(payment.getPaymentId())
                .orElse(VnpayTransaction.builder().payment(payment).build());

        tx.setVnpTxnRef(params.get("vnp_TxnRef"));
        tx.setVnpTransactionNo(params.get("vnp_TransactionNo"));
        tx.setVnpResponseCode(params.get("vnp_ResponseCode"));
        tx.setVnpTransactionStatus(params.get("vnp_TransactionStatus"));

        tx.setIpnQuery(query);
        tx.setIpnSecureHash(secureHash);
        tx.setIpnVerified(verified);
        tx.setIpnReceivedAt(LocalDateTime.now());
        tx.setIpnParamsJson(writeJson(params));

        vnpayTransactionRepository.save(tx);
    }

    private String writeJson(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception ex) {
            return null;
        }
    }
}
