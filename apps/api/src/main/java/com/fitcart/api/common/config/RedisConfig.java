package com.fitcart.api.common.config;

import com.fitcart.api.common.cache.CacheNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(name = "fitcart.cache.enabled", havingValue = "false")
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    @ConditionalOnProperty(name = {
            "fitcart.cache.enabled",
            "fitcart.cache.redis-enabled"
    }, havingValue = "true")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            CacheProperties cacheProperties,
            ObjectMapper objectMapper
    ) {
        ObjectMapper cacheObjectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(cacheObjectMapper)
                ));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                CacheNames.ADVISOR_TOP_PRODUCTS,
                defaultConfiguration.entryTtl(cacheProperties.getAdvisorTopProductsTtl()),
                CacheNames.AUTOCOMPLETE_RESULTS,
                defaultConfiguration.entryTtl(cacheProperties.getAutocompleteTtl())
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnExpression("${fitcart.cache.enabled:true} and !${fitcart.cache.redis-enabled:true}")
    public CacheManager inMemoryCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                CacheNames.ADVISOR_TOP_PRODUCTS,
                CacheNames.AUTOCOMPLETE_RESULTS
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
