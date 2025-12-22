package com.mindwell.be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VnpayConfig {

    @Value("${app.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.vnpay.hash-secret}")
    private String hashSecret;

    @Value("${app.vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${app.vnpay.api-version:2.1.0}")
    private String apiVersion;

    @Value("${app.vnpay.locale:vn}")
    private String locale;

    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getLocale() {
        return locale;
    }
}
