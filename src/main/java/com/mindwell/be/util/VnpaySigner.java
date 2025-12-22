package com.mindwell.be.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class VnpaySigner {

    private VnpaySigner() {
    }

    public static String hmacSHA512(String key, String data) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Missing VNPAY hash secret");
        }
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute HMAC-SHA512", ex);
        }
    }

    public record SignedQuery(String queryString, String hashData, String secureHash) {
    }

    /**
     * Build VNPAY query string and secure hash.
     * - Sort keys ascending
     * - URL-encode keys and values (UTF-8)
     * - Skip null/blank values
     *
     * IMPORTANT: VNPAY sample (NodeJS) hashes the exact query string representation
     * (encoded keys + encoded values) before appending vnp_SecureHash.
     */
    public static SignedQuery sign(Map<String, String> params, String hashSecret) {
        if (params == null) params = Map.of();

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        boolean first = true;
        for (String fieldName : fieldNames) {
            if (fieldName == null || fieldName.isBlank()) continue;
            String fieldValue = params.get(fieldName);
            if (fieldValue == null || fieldValue.isBlank()) continue;

            String encName = URLEncoder.encode(fieldName, StandardCharsets.UTF_8);
            String encValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);

            if (!first) {
                query.append('&');
                hashData.append('&');
            }

            query.append(encName).append('=').append(encValue);
            hashData.append(encName).append('=').append(encValue);
            first = false;
        }

        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        return new SignedQuery(query.toString(), hashData.toString(), secureHash);
    }

    public static boolean verify(Map<String, String> params, String providedSecureHash, String hashSecret) {
        if (providedSecureHash == null || providedSecureHash.isBlank()) return false;

        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getKey() == null) continue;
            String key = e.getKey();
            if ("vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key)) {
                continue;
            }
            filtered.put(key, e.getValue());
        }

        String expected = sign(filtered, hashSecret).secureHash();
        return expected.equalsIgnoreCase(providedSecureHash);
    }
}
