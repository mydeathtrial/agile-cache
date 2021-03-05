package cloud.agileframework.cache.support;

import org.springframework.cache.Cache;

/**
 * @author 佟盟
 * 日期 2019/7/23 17:55
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public interface AgileCacheManagerInterface {
    /**
     * 包装、转换，把spring-cache转换为AgileCache
     *
     * @param cache spring-cache
     * @return agileCache
     */
    AgileCache cover(Cache cache);

    /**
     * 获取不存在的缓存
     *
     * @param cacheName 缓存名
     * @return agileCache
     */
    AgileCache getMissingCache(String cacheName);

    /**
     * 根据名字获取缓存
     *
     * @param cacheName 缓存名
     * @return agileCache
     */
    AgileCache getCache(String cacheName);
}
