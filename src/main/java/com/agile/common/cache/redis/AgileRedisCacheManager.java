package com.agile.common.cache.redis;

import com.agile.common.cache.AgileCache;
import com.agile.common.cache.AgileCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Component
@ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "redis")
public class AgileRedisCacheManager extends AgileCacheManager {

    private static RedisCacheManager cacheManager;
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public AgileRedisCacheManager(RedisCacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
        setCacheManager(cacheManager);
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public static RedisCacheManager getCacheManager() {
        return cacheManager;
    }

    public static void setCacheManager(RedisCacheManager cacheManager) {
        AgileRedisCacheManager.cacheManager = cacheManager;
    }

    @Override
    public AgileCache cover(Cache cache) {
        return new AgileRedis((RedisCache) cache, redisConnectionFactory);
    }

    @Override
    public AgileCache getMissingCache(String cacheName) {
        return cover(cacheManager.getCache(cacheName));
    }

}
