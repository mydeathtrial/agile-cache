package com.agile.common.cache;

import org.springframework.cache.Cache;

/**
 * @author 佟盟
 * 日期 2019/7/23 17:55
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public interface AgileCacheManagerInterface {
    AgileCache cover(Cache cache);

    AgileCache getMissingCache(String cacheName);

    AgileCache getCache(String cacheName);
}
