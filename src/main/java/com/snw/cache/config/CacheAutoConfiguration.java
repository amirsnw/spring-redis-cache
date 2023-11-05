package com.snw.cache.config;

import com.snw.cache.CacheHelper;
import com.snw.cache.CacheHelperImpl;
import com.snw.cache.RedisJsonSerializer;
import com.snw.cache.UnifiedRedisCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;


@Configuration
@EnableCaching
@EnableConfigurationProperties({CacheSettings.class})
public class CacheAutoConfiguration {

    @Bean
    public CacheHelper cacheHelper(CacheManager cacheManager) {
        //Expose an instance of the cache helper.
        return new CacheHelperImpl(cacheManager);
    }

    //This caching library is only enabled when the cache type is set to Redis.
    @ConditionalOnExpression("'${spring.cache.type:redis}' == 'redis'")
    protected static class CacheEnabledConfiguration {

        //We add a meter binder provider to allow metrics to be published on cache hits, misses, promotions, and puts.
        @Bean
        public UnifiedRedisCacheMeterBinderProvider unifiedRedisCacheMeterBinderProvider() {
            return new UnifiedRedisCacheMeterBinderProvider();
        }

        @Bean
        public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<Object, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            RedisJsonSerializer jsonRedisSerializer = new RedisJsonSerializer();
            template.setKeySerializer(jsonRedisSerializer);
            template.setValueSerializer(jsonRedisSerializer);
            return template;
        }

        @Bean(name = {"cacheManager"})
        public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate, @Lazy CacheMetricsRegistrar registrar, CacheSettings cacheSettings,
                                         @Value("${info.build.version:1.0.0-SNAPSHOT}") String applicationVersion) {

            return new UnifiedRedisCacheManager(redisTemplate, registrar, cacheSettings, applicationVersion);
        }
    }

}
