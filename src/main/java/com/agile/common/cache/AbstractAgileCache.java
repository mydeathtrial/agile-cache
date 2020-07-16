package com.agile.common.cache;

import org.springframework.cache.Cache;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.Callable;

/**
 * @author 佟盟
 * 日期 2019/7/23 18:19
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractAgileCache implements AgileCache {
    protected Cache cache;

    public AbstractAgileCache(Cache cache) {
        this.cache = cache;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void put(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        try {
            return cache.get(key, clazz);
        } catch (SerializationException e) {
            cache.evict(key);
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        cache.evict(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            return cache.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return cache.get(key, valueLoader);
    }
}
