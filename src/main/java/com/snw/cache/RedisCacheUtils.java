package com.snw.cache;

import org.springframework.data.redis.core.RedisOperations;

import java.util.Arrays;

public final class RedisCacheUtils {

    private RedisCacheUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static byte[] computeKey(RedisOperations template, byte[] prefix, Object key) {
        if (key instanceof byte[] var) {
            return var;
        }
        byte[] k = template.getKeySerializer().serialize(key);

        if (prefix == null || prefix.length == 0) {
            return k;
        }

        byte[] result = Arrays.copyOf(prefix, prefix.length + k.length);
        System.arraycopy(k, 0, result, prefix.length, k.length);
        return result;
    }

}
