package com.qrgen.util;

/**
 * Builds correctly formatted payload strings for the various "content types"
 * the app supports, following the conventions most QR scanner apps expect
 * (e.g. the WIFI: and MECARD: schemes, mailto:/sms: URIs).
 */
public final class ContentBuilder {

    private ContentBuilder() {
    }

    public static String plainText(String text) {
        return text;
    }

    public static String url(String url) {
        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        boolean hasScheme = trimmed.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*");
        return hasScheme ? trimmed : "https://" + trimmed;
    }

    public static String email(String address, String subject, String body) {
        StringBuilder sb = new StringBuilder("mailto:").append(escape(address));
        boolean first = true;
        if (subject != null && !subject.isEmpty()) {
            sb.append(first ? '?' : '&').append("subject=").append(encodeParam(subject));
            first = false;
        }
        if (body != null && !body.isEmpty()) {
            sb.append(first ? '?' : '&').append("body=").append(encodeParam(body));
        }
        return sb.toString();
    }

    public static String phone(String number) {
        return "tel:" + escape(number);
    }

    public static String sms(String number, String message) {
        StringBuilder sb = new StringBuilder("sms:").append(escape(number));
        if (message != null && !message.isEmpty()) {
            sb.append("?body=").append(encodeParam(message));
        }
        return sb.toString();
    }

    /**
     * Builds a WIFI: payload understood by most phone camera apps.
     *
     * @param ssid       network name
     * @param password   network password (ignored if security is "nopass")
     * @param security   one of "WPA", "WEP", "nopass"
     * @param hidden     whether the network is hidden
     */
    public static String wifi(String ssid, String password, String security, boolean hidden) {
        StringBuilder sb = new StringBuilder("WIFI:");
        sb.append("T:").append(security == null ? "WPA" : security).append(';');
        sb.append("S:").append(escapeWifi(ssid)).append(';');
        if (!"nopass".equalsIgnoreCase(security) && password != null && !password.isEmpty()) {
            sb.append("P:").append(escapeWifi(password)).append(';');
        }
        if (hidden) {
            sb.append("H:true;");
        }
        sb.append(';');
        return sb.toString();
    }

    /**
     * Builds a MECARD-style vCard-lite payload for a contact card.
     */
    public static String contact(String name, String phoneNumber, String email, String org) {
        StringBuilder sb = new StringBuilder("MECARD:");
        if (name != null && !name.isEmpty()) {
            sb.append("N:").append(escapeWifi(name)).append(';');
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sb.append("TEL:").append(escapeWifi(phoneNumber)).append(';');
        }
        if (email != null && !email.isEmpty()) {
            sb.append("EMAIL:").append(escapeWifi(email)).append(';');
        }
        if (org != null && !org.isEmpty()) {
            sb.append("ORG:").append(escapeWifi(org)).append(';');
        }
        sb.append(';');
        return sb.toString();
    }

    private static String escape(String s) {
        return s == null ? "" : s.trim();
    }

    private static String escapeWifi(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:");
    }

    private static String encodeParam(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
