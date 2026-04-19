package com.fitcart.api.common.cache;

import com.fitcart.api.autocomplete.domain.AutocompleteType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public final class CacheKeyBuilder {

    private CacheKeyBuilder() {
    }

    public static String advisorQuery(String query, Integer topK) {
        return "query:" + sha256(normalize(query)) + ":topK:" + (topK == null ? 3 : topK);
    }

    public static String autocomplete(String query, Set<AutocompleteType> types, int limit) {
        String typeToken = (types == null || types.isEmpty())
                ? "all"
                : types.stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(","));

        return "query:" + sha256(normalize(query)) + ":types:" + typeToken + ":limit:" + limit;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
