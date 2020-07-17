package com.agile.common.cache.memory;

import com.agile.common.cache.AgileCache;
import com.agile.common.cache.AgileCacheManager;
import org.springframework.cache.Cache;

/**
 * @author 佟盟
 * 日期 2020/7/17 10:41
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class MemoryCacheManager extends AgileCacheManager {

    @Override
    public AgileCache cover(Cache cache) {
        return null;
    }

    @Override
    public AgileCache getMissingCache(String cacheName) {
        return new MemoryCache(cacheName);
    }
}
