package com.mindwell.be.util;

import jakarta.servlet.http.HttpServletRequest;

public final class IpAddressUtils {

    private IpAddressUtils() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // XFF can be a list: client, proxy1, proxy2
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }

        String ip = request.getRemoteAddr();
        return (ip == null || ip.isBlank()) ? "127.0.0.1" : ip;
    }
}
