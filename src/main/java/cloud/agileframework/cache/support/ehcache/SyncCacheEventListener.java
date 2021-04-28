package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.RedisSyncCache;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.sync.SyncKeys;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-03-24 10:40
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class SyncCacheEventListener extends CacheEventListenerAdapter {
    private SyncCache syncCache;

    @Autowired
    public void setSyncCache(SyncCache syncCache) {
        this.syncCache = syncCache;
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        syncCache.sync(getSyncKeys(cache, element), OpType.DELETE);
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        syncCache.sync(getSyncKeys(cache, element), OpType.WRITE);
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        syncCache.sync(getSyncKeys(cache, element), OpType.WRITE);
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        syncCache.sync(getSyncKeys(cache, element), OpType.DELETE);
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        List keys = cache.getKeys();
        for (Object key : keys) {
            syncCache.sync(SyncKeys.of(cache.getName(), key), OpType.DELETE);
        }
    }

    /**
     * 获取同步key信息
     *
     * @param cache   缓存区域
     * @param element 元素
     * @return 需要同步的key信息
     */
    private SyncKeys getSyncKeys(Ehcache cache, Element element) {
        if (element.getTimeToLive() == 0) {
            return SyncKeys.of(cache.getName(), element.getObjectKey());
        } else {
            final long millis = element.getExpirationTime() - System.currentTimeMillis();
            if (millis > 0) {
                return SyncKeys.of(cache.getName(), element.getObjectKey(), Duration.ofMillis(millis));
            }
            return SyncKeys.of(cache.getName(), element.getObjectKey(), Duration.ofMillis(0));
        }
    }
}
