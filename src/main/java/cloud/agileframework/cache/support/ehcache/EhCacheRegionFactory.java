package cloud.agileframework.cache.support.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.ehcache.internal.SingletonEhcacheRegionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 佟盟 on 2018/1/4
 */
public class EhCacheRegionFactory extends SingletonEhcacheRegionFactory {
    private final Logger logger = LoggerFactory.getLogger(EhCacheRegionFactory.class);
    private static final AtomicInteger REFERENCE_COUNT = new AtomicInteger();

    @Override
    protected CacheManager resolveCacheManager(SessionFactoryOptions settings, Map properties) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("完成初始化EhCache二级缓存区域");
            }
            REFERENCE_COUNT.incrementAndGet();
            return AgileEhCacheCacheManager.getCacheManager();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("初始化EhCache二级缓存区域失败", e);
            }
            REFERENCE_COUNT.decrementAndGet();
            throw e;
        }
    }

    @Override
    protected Cache createCache(String regionName) {
        CacheManager cacheManager = AgileEhCacheCacheManager.getCacheManager();
        assert cacheManager != null;
        return (Cache) cacheManager.addCacheIfAbsent(regionName);
    }
}
