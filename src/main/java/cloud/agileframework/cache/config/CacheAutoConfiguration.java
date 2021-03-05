package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AbstractAgileCacheManager;

/**
 * @author 佟盟
 * 日期 2020-12-18 10:36
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public interface CacheAutoConfiguration {
    /**
     * 提供Agile的缓存管理器
     *
     * @return Agile的缓存管理器
     */
    AbstractAgileCacheManager agileCacheManager();
}
