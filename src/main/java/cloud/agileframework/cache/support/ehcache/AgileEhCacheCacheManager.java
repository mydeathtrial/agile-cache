package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.support.AgileCacheManager;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class AgileEhCacheCacheManager extends AgileCacheManager {

    private static EhCacheCacheManager cacheManager;

    public AgileEhCacheCacheManager(EhCacheCacheManager cacheManager) {
        setCacheManager(cacheManager);
    }

    public static EhCacheCacheManager getCacheManager() {
        return cacheManager;
    }

    public static void setCacheManager(EhCacheCacheManager cacheManager) {
        AgileEhCacheCacheManager.cacheManager = cacheManager;
    }

    @Override
    public AgileCache cover(Cache cache) {
        return new AgileEhCache((EhCacheCache) cache);
    }

    @Override
    public AgileCache getMissingCache(String cacheName) {
        CacheManager ehcacheCacheManager = cacheManager.getCacheManager();
        assert ehcacheCacheManager != null;
        Ehcache target = ehcacheCacheManager.getEhcache(cacheName);
        if (target == null) {
            target = ehcacheCacheManager.addCacheIfAbsent(cacheName);
        }
        return cover(new EhCacheCache(target));
    }
}
