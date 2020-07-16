package com.agile.common.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 佟盟
 * 日期 2019/7/23 17:06
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public abstract class AgileCacheManager implements AgileCacheManagerInterface {
    private static final ConcurrentMap<String, AgileCache> CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public AgileCache getCache(String cacheName) {
        AgileCache cache = CACHE_MAP.get(cacheName);
        if (cache == null) {
            cache = getMissingCache(cacheName);
            CACHE_MAP.putIfAbsent(cacheName, cache);
        }
        return cache;
    }

}
