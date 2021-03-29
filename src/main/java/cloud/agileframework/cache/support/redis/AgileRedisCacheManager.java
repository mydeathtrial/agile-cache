package cloud.agileframework.cache.support.redis;

import cloud.agileframework.cache.support.AbstractAgileCacheManager;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */

public class AgileRedisCacheManager extends AbstractAgileCacheManager {

    private RedisCacheManager cacheManager;
    private final RedisConnectionFactory redisConnectionFactory;

    public AgileRedisCacheManager(RedisCacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
        setCacheManager(cacheManager);
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public RedisCacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public AgileRedis cover(Cache cache) {
        return new AgileRedis((RedisCache) cache, redisConnectionFactory);
    }

    @Override
    public AgileRedis getMissingCache(String cacheName) {
        return cover(cacheManager.getCache(cacheName));
    }

    private final ConcurrentMap<String, AgileRedis> CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public AgileRedis getCache(String cacheName) {
        AgileRedis cache = CACHE_MAP.get(cacheName);
        if (cache == null) {
            cache = getMissingCache(cacheName);
            CACHE_MAP.putIfAbsent(cacheName, cache);
        }
        return cache;
    }
}
