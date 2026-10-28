package com.hiresmart.util;

import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

public class JdbcUtils {

    public static Long getGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;
        try {
            Number key = keyHolder.getKey();
            if (key != null) return key.longValue();
        } catch (Exception ignored) {
            // Falls through if driver returns multiple keys (e.g., id + timestamps)
        }

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null) {
            for (Map.Entry<String, Object> entry : keys.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("id") && entry.getValue() instanceof Number n) {
                    return n.longValue();
                }
            }
            for (Object val : keys.values()) {
                if (val instanceof Number n) {
                    return n.longValue();
                }
            }
        }

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> first = keyHolder.getKeyList().get(0);
            for (Map.Entry<String, Object> entry : first.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("id") && entry.getValue() instanceof Number n) {
                    return n.longValue();
                }
            }
            for (Object val : first.values()) {
                if (val instanceof Number n) {
                    return n.longValue();
                }
            }
        }

        return null;
    }
}
