package com.lanracing.Networking;

import java.util.HashMap;
import java.util.Map;

public final class MessageHandler {
    private MessageHandler() {}

    public static String format(String type, Map<String, String> payload) {
        StringBuilder sb = new StringBuilder(type);
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            sb.append('|').append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    public static ParsedMessage parse(String raw) {
        String[] parts = raw.split("\\|");
        String type = parts.length > 0 ? parts[0] : "UNKNOWN";
        Map<String, String> payload = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];
            int idx = token.indexOf('=');
            if (idx > 0) {
                payload.put(token.substring(0, idx), token.substring(idx + 1));
            }
        }
        return new ParsedMessage(type, payload);
    }

    public static class ParsedMessage {
        private final String type;
        private final Map<String, String> payload;

        public ParsedMessage(String type, Map<String, String> payload) {
            this.type = type;
            this.payload = payload;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getPayload() {
            return payload;
        }
    }
}
