package com.agile.common.cache.ehcache;

import com.agile.common.cache.AbstractAgileCache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.util.NumberUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 佟盟
 * 日期 2019/7/23 18:17
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class AgileEhCache extends AbstractAgileCache {


    AgileEhCache(EhCacheCache cache) {
        super(cache);
    }

    private Ehcache getEhCache() {
        return (Ehcache) cache.getNativeCache();
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        Ehcache ehCache = getEhCache();
        Element element = new Element(key, value);
        element.setTimeToLive(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
        element.setTimeToIdle(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
        element.setEternal(true);
        ehCache.put(element);
    }

    @Override
    public boolean containKey(Object key) {
        return getEhCache().get(key) != null;
    }

    private Map<Object, Object> getMap(Object mapKey, boolean require) {
        Element value = getEhCache().get(mapKey);
        if (value == null) {
            if (require) {
                getEhCache().put(new Element(mapKey, new HashMap<>()));
            } else {
                throw new RuntimeException("Cache data does not exist");
            }
        }
        Object map = value.getObjectValue();
        if (!Map.class.isAssignableFrom(map.getClass())) {
            throw new RuntimeException("Target data is not the expected type");
        }

        return (Map) map;
    }

    private List<Object> getList(Object listKey, boolean require) {
        Element value = getEhCache().get(listKey);
        if (value == null) {
            if (require) {
                getEhCache().put(new Element(listKey, new ArrayList<>()));
            } else {
                throw new RuntimeException("Cache data does not exist");
            }
        }
        Object map = value.getObjectValue();
        if (!List.class.isAssignableFrom(map.getClass())) {
            throw new RuntimeException("Target data is not the expected type");
        }
        return (List) map;
    }

    private Set<Object> getSet(Object setKey, boolean require) {
        Element value = getEhCache().get(setKey);
        if (value == null) {
            if (require) {
                getEhCache().put(new Element(setKey, new HashSet<>()));
            } else {
                throw new RuntimeException("Cache data does not exist");
            }
        }
        Object map = value.getObjectValue();
        if (!Set.class.isAssignableFrom(map.getClass())) {
            throw new RuntimeException("Target data is not the expected type");
        }
        return (Set) map;
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {
        Map<Object, Object> map = getMap(mapKey, true);
        map.put(key, value);
    }

    @Override
    public Object getFromMap(Object mapKey, Object key) {
        Map<Object, Object> map = getMap(mapKey, false);
        return map.get(key);
    }

    @Override
    public <T> T getFromMap(Object mapKey, Object key, Class<T> clazz) {
        Object value = getFromMap(mapKey, key);
        if (value != null && clazz != null && !clazz.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + clazz.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    public void removeFromMap(Object mapKey, Object key) {
        Map<Object, Object> map = getMap(mapKey, false);
        map.remove(key);
    }

    @Override
    public void addToList(Object listKey, Object node) {
        List<Object> list = getList(listKey, true);
        list.add(node);
    }

    @Override
    public Object getFromList(Object listKey, int index) {
        List<Object> list = getList(listKey, false);
        return list.get(index);
    }

    @Override
    public <T> T getFromList(Object listKey, int index, Class<T> clazz) {
        Object value = getFromList(listKey, index);
        if (value != null && clazz != null && !clazz.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + clazz.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    public void removeFromList(Object listKey, int index) {
        List<Object> list = getList(listKey, false);
        list.remove(index);
    }

    @Override
    public void addToSet(Object setKey, Object node) {
        Set<Object> set = getSet(setKey, true);
        set.add(node);
    }

    @Override
    public void removeFromSet(Object setKey, Object node) {
        Set<Object> set = getSet(setKey, false);
        set.remove(node);
    }

    @Override
    public boolean lock(Object lock) {
        Ehcache ehcache = getEhCache();
        boolean isLock;
        try {
            isLock = ehcache.tryWriteLockOnKey(lock, 0);
        } catch (InterruptedException e) {
            isLock = false;
        }
        if (isLock) {
            ehcache.acquireWriteLockOnKey(lock);
            ehcache.put(new Element(lock, new byte[0]));
        }

        return isLock;
    }

    @Override
    public boolean lock(Object lock, Duration timeout) {
        Ehcache ehcache = getEhCache();
        boolean isLock;
        try {
            isLock = ehcache.tryWriteLockOnKey(lock, 0);
        } catch (InterruptedException e) {
            isLock = false;
        }
        if (isLock) {
            ehcache.acquireWriteLockOnKey(lock);
            ehcache.put(new Element(lock, new byte[0], timeout.getSeconds()));
        }

        return isLock;
    }

    @Override
    public void unlock(Object lock) {
        Ehcache ehcache = getEhCache();
        ehcache.releaseReadLockOnKey(lock);
    }

    @Override
    public void unlock(Object lock, Duration timeout) {
        Ehcache ehcache = getEhCache();
        ehcache.put(new Element(lock, new byte[0], timeout.getSeconds()));
    }

}
