package com.snw.cache;

import com.snw.cache.config.CacheSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.util.*;


public class UnifiedRedisCacheManager extends AbstractTransactionSupportingCacheManager {

    private final RedisOperations<?, ?> redisOperations;
    private final String applicationVersion;

    // Should key values be prefixed with their cache name?
    private final boolean useCacheNamePrefix;
    private final StringRedisSerializer cacheNamePrefixSerializer = new StringRedisSerializer();

    // If using cache name prefixes, the delimiter is the string that separates the cache name from the actual key value.
    private final String keyDelimiter;
    // The default time to live is 1 day.
    private final long defaultTimeToLive;
    // Allow caches to be added dynamically
    private boolean dynamic = true;
    // List of caches to pre-create.
    private Set<String> configuredCacheNames;
    // List of specific cache TTL overrides.
    private Map<String, Long> expires = null;

    public UnifiedRedisCacheManager(RedisOperations<?, ?> redisOperations,
                                    CacheMetricsRegistrar registrar,
                                    CacheSettings cacheSettings,
                                    String applicationVersion) {

        CacheSettings.Redis redisProperties = cacheSettings.getRedis();
        this.redisOperations = redisOperations;
        this.applicationVersion = applicationVersion;

        if (redisProperties.getTimeToLive() != null) {
            defaultTimeToLive = redisProperties.getTimeToLive().getSeconds();
        } else {
            // If not defined, the default is 1 day.
            defaultTimeToLive = 86400;
        }

        // Should the key values be prefixed with the cache name?
        if (!redisProperties.isUseKeyPrefix()) {
            useCacheNamePrefix = false;
            keyDelimiter = null;
        } else {
            // If so, the format will be <CACHE_NAME><DELIMITER><KEY_VALUE>
            useCacheNamePrefix = true;
            if (StringUtils.isBlank(redisProperties.getKeyPrefix())) {
                keyDelimiter = ":";
            } else {
                keyDelimiter = redisProperties.getKeyPrefix();
            }
        }
        expires = cacheSettings.getExpirations();
    }

    protected UnifiedRedisCache createCache(String cacheName) {
        long expiration = computeExpiration(cacheName);
        return new UnifiedRedisCache(cacheName, useCacheNamePrefix ? computeCacheNamePrefix(cacheName) : null,
                redisOperations, expiration, applicationVersion);
    }

    private byte[] computeCacheNamePrefix(String cacheName) {
        return cacheNamePrefixSerializer.serialize(cacheName.concat(keyDelimiter));
    }

    protected long computeExpiration(String name) {
        Long expiration = null;
        if (expires != null) {
            expiration = expires.get(name);
        }
        return (expiration != null ? expiration : defaultTimeToLive);
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {


        if (this.configuredCacheNames == null) {
            return Collections.emptySet();
        }
        Assert.notNull(this.redisOperations, "A redis template is required in order to interact with data store");

        Set<Cache> caches = new HashSet<>();

        for (String cacheName : this.configuredCacheNames) {
            caches.add(createCache(cacheName));
        }
        return caches;
    }

    @Override
    protected Cache getMissingCache(String name) {
        if (!this.dynamic) {
            return null;
        }

        return createCache(name);
    }

    /* (non-Javadoc)
    * @see
    org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager#decorateCache(org.springframework.cache.Cache)
    */
    @Override
    protected Cache decorateCache(Cache cache) {

        if (isCacheAlreadyDecorated(cache)) {
            return cache;
        }

        return super.decorateCache(cache);
    }

    protected boolean isCacheAlreadyDecorated(Cache cache) {
        return isTransactionAware() && cache instanceof TransactionAwareCacheDecorator;
    }
}
