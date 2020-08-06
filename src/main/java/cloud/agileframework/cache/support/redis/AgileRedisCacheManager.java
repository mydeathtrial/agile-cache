package cloud.agileframework.cache.support.redis;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.support.AgileCacheManager;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */

public class AgileRedisCacheManager extends AgileCacheManager {

    private static RedisCacheManager cacheManager;
    private final RedisConnectionFactory redisConnectionFactory;

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
