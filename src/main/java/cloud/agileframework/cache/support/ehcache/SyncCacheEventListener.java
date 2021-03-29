package cloud.agileframework.cache.support.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

/**
 * @author 佟盟
 * 日期 2021-03-24 10:40
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class SyncCacheEventListener extends CacheEventListenerAdapter {
    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        System.out.println("放了");
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        System.out.println("放了");
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        System.out.println("放了");
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        System.out.println("放了");
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        System.out.println("放了");
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        System.out.println("放了");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public void dispose() {
        System.out.println("放了");
    }
}
