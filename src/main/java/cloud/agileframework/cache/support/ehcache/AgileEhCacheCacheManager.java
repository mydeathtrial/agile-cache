package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AbstractAgileCacheManager;
import cloud.agileframework.cache.support.AgileCache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class AgileEhCacheCacheManager extends AbstractAgileCacheManager {

    private static CacheManager cacheManager;

    public AgileEhCacheCacheManager(CacheManager cacheManager) {
        setCacheManager(cacheManager);
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    public static void setCacheManager(CacheManager cacheManager) {
        AgileEhCacheCacheManager.cacheManager = cacheManager;
    }

    @Override
    public AgileCache cover(Cache cache) {
        return new AgileEhCache((EhCacheCache) cache);
    }

    @Override
    public AgileCache getMissingCache(String cacheName) {
        assert cacheManager != null;
        Ehcache target = cacheManager.getEhcache(cacheName);
        if (target == null) {
            target = cacheManager.addCacheIfAbsent(cacheName);
        }
        return cover(new EhCacheCache(target));
    }
}
