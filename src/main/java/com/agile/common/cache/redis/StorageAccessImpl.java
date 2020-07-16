package com.agile.common.cache.redis;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCache;

/**
 * @author 佟盟 on 2018/5/10
 */
public class StorageAccessImpl implements DomainDataStorageAccess {
    private final Logger logger = LoggerFactory.getLogger(StorageAccessImpl.class);
    private final RedisCache cache;

    StorageAccessImpl(RedisCache cache) {
        this.cache = cache;
    }

    public RedisCache getCache() {
        return cache;
    }

    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
        try {
            final Cache.ValueWrapper element = getCache().get(key);
            if (element == null) {
                return null;
            } else {
                return element.get();
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("连接Redis失败", e);
            throw new CacheException(e);
        } catch (Exception e) {
            logger.error("redis缓存提取失败", e);
            throw new CacheException(e);
        }
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        try {
            getCache().put(key, value);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new CacheException(e);
        } catch (Exception e) {
            logger.error("redis缓存存放失败", e);
            throw new CacheException(e);
        }
    }

    @Override
    public boolean contains(Object key) {
        return getCache().get(key) != null;
    }

    @Override
    public void evictData() {
        try {
            getCache().clear();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("redis缓存清空数据失败", e);
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void evictData(Object key) {
        try {
            getCache().get(key);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("redis缓存删除数据失败", e);
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void release() {
        try {
            getCache().clear();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                if (e instanceof RedisConnectionFailureException) {
                    logger.error("redis连接失败", e);
                } else {
                    logger.error("redis缓存删除数据失败", e);
                }
            }
            throw new CacheException(e);
        }
    }
}
