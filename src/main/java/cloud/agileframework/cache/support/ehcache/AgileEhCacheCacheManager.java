package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AbstractAgileCacheManager;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.event.CacheEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 佟盟
 * 日期 2019/7/22 17:14
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class AgileEhCacheCacheManager extends AbstractAgileCacheManager {

    private CacheManager cacheManager;
    private CacheEventListener syncCacheEventListener;

    public AgileEhCacheCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    public void setSyncCacheEventListener(CacheEventListener syncCacheEventListener) {
        this.syncCacheEventListener = syncCacheEventListener;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public AgileEhCache cover(Cache cache) {
        return new AgileEhCache((EhCacheCache) cache);
    }

    @Override
    public AgileEhCache getMissingCache(String cacheName) {
        assert cacheManager != null;
        Ehcache target = cacheManager.getEhcache(cacheName);
        if (target == null) {
            target = cacheManager.addCacheIfAbsent(cacheName);
        }
        return cover(new EhCacheCache(target));
    }

    private static final ConcurrentMap<String, AgileEhCache> CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public AgileEhCache getCache(String cacheName) {
        AgileEhCache cache = CACHE_MAP.get(cacheName);
        if (cache == null) {
            cache = getMissingCache(cacheName);
            CACHE_MAP.putIfAbsent(cacheName, cache);
            cache.getNativeCache().getCacheEventNotificationService().registerListener(syncCacheEventListener);
        }

        return cache;
    }
}
