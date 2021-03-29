package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AbstractAgileCache;
import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.spring.util.AsyncUtil;
import cloud.agileframework.spring.util.BeanUtil;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.NumberUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    @Override
    public Ehcache getNativeCache() {
        return (Ehcache) super.getNativeCache();
    }

    private SyncCache syncCache() {
        return BeanUtil.getBean(SyncCache.class);
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        return syncCache().sync(getName(), key.toString(), () -> {
            ValueWrapper v = super.get(key);
            if (v == null) {
                return null;
            }
            final Object o = v.get();

            if (o == null) {
                return null;
            }
            if (o.getClass() == clazz) {
                return (T) o;
            } else {
                return ObjectUtil.to(o, new TypeReference<>(clazz));
            }
        }, OpType.READ);
    }

    @Override
    public ValueWrapper get(Object key) {
        return syncCache().sync(getName(), key.toString(), () -> super.get(key), OpType.READ);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return syncCache().sync(getName(), key.toString(), () -> super.get(key, valueLoader), OpType.READ);
    }

    @Override
    public <T> T get(Object key, TypeReference<T> typeReference) {
        return syncCache().sync(getName(), key.toString(), () -> super.get(key, typeReference), OpType.READ);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return syncCache().sync(getName(), key.toString(), () -> super.evictIfPresent(key), OpType.WRITE);
    }

    @Override
    public boolean invalidate() {
        boolean notEmpty = getNativeCache().getSize() > 0;
        syncCache().clear(getName());
        return notEmpty;
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        syncCache().sync(getName(), key.toString(), () -> {
            Ehcache ehCache = getNativeCache();
            Element element = new Element(key, value);
            element.setTimeToLive(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
            element.setTimeToIdle(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
            element.setEternal(true);

            Element old = ehCache.get(key);
            if (old == null) {
                ehCache.put(element);
            } else {
                ehCache.replace(old, element);
            }
            //主动失效
            AsyncUtil.execute(() -> evict(key), timeout);
            return null;
        }, OpType.WRITE);
    }

    @Override
    public void put(Object key, Object value) {
        //内存缓存保留28~32分钟之间的随机值，防止穿透，
        long randomRange = RandomUtils.nextLong(1680000, 1920000);
        final Duration timeout = Duration.ofMillis(randomRange);
        syncCache().sync(getName(), key.toString(), () -> {
            Ehcache ehCache = getNativeCache();
            Element element = new Element(key, value);
            element.setTimeToLive(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
            element.setTimeToIdle(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
            element.setEternal(true);

            Element old = ehCache.get(key);
            if (old == null) {
                ehCache.put(element);
            } else {
                ehCache.replace(old, element);
            }
            return null;
        }, OpType.WRITE);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return syncCache().sync(getName(), key.toString(), () -> super.putIfAbsent(key, value), OpType.WRITE);
    }

    @Override
    public void evict(Object key) {
        syncCache().sync(getName(), key.toString(), () -> {
            super.evict(key);
            return null;
        }, OpType.DELETE);
    }

    @Override
    public boolean containKey(Object key) {
        return syncCache().sync(getName(), key.toString(), () -> getNativeCache().get(key) != null, OpType.READ);
    }

    private Map<Object, Object> getMap(Object mapKey, boolean require) {
        return syncCache().sync(getName(), mapKey.toString(), () -> {
            Element value = getNativeCache().get(mapKey);
            if (value == null) {
                if (require) {
                    value = new Element(mapKey, new HashMap<>(0));
                    getNativeCache().put(value);
                } else {
                    throw new CacheException("Cache data does not exist");
                }
            }
            Object map = value.getObjectValue();
            if (!Map.class.isAssignableFrom(map.getClass())) {
                throw new CacheException("Target data is not the expected type");
            }

            return (Map<Object, Object>) map;
        }, OpType.READ);
    }

    private List<Object> getList(Object listKey, boolean require) {
        return syncCache().sync(getName(), listKey.toString(), () -> {
            Element value = getNativeCache().get(listKey);
            if (value == null) {
                if (require) {
                    value = new Element(listKey, new ArrayList<>());
                    getNativeCache().put(value);
                } else {
                    throw new CacheException("Cache data does not exist");
                }
            }
            Object map = value.getObjectValue();
            if (!List.class.isAssignableFrom(map.getClass())) {
                throw new CacheException("Target data is not the expected type");
            }
            return (List) map;
        }, OpType.READ);

    }

    private Set<Object> getSet(Object setKey, boolean require) {
        return syncCache().sync(getName(), setKey.toString(), () -> {
            Element value = getNativeCache().get(setKey);
            if (value == null) {
                if (require) {
                    value = new Element(setKey, new HashSet<>());
                    getNativeCache().put(value);
                } else {
                    throw new CacheException("Cache data does not exist");
                }
            }
            Object map = value.getObjectValue();
            if (!Set.class.isAssignableFrom(map.getClass())) {
                throw new CacheException("Target data is not the expected type");
            }
            return (Set) map;
        }, OpType.READ);
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {
        syncCache().sync(getName(), mapKey.toString(), () -> {
            Map<Object, Object> map = getMap(mapKey, true);
            map.put(key, value);
            return null;
        }, OpType.WRITE);

    }

    @Override
    public Object getFromMap(Object mapKey, Object key) {
        return syncCache().sync(getName(), mapKey.toString(), () -> {
            Map<Object, Object> map = getMap(mapKey, false);
            return map.get(key);
        }, OpType.READ);
    }

    @Override
    public <T> T getFromMap(Object mapKey, Object key, Class<T> clazz) {
        return syncCache().sync(getName(), mapKey.toString(), () -> {
            Object value = getFromMap(mapKey, key);
            if (value != null && clazz != null && !clazz.isInstance(value)) {
                throw new IllegalStateException(
                        "Cached value is not of required type [" + clazz.getName() + "]: " + value);
            }
            return (T) value;
        }, OpType.READ);
    }

    @Override
    public void removeFromMap(Object mapKey, Object key) {
        syncCache().sync(getName(), mapKey.toString(), () -> {
            Map<Object, Object> map = getMap(mapKey, false);
            map.remove(key);
            return null;
        }, OpType.DELETE);
    }

    @Override
    public void addToList(Object listKey, Object node) {
        syncCache().sync(getName(), listKey.toString(), () -> {
            List<Object> list = getList(listKey, true);
            list.add(node);
            return null;
        }, OpType.DELETE);
    }

    @Override
    public Object getFromList(Object listKey, int index) {
        return syncCache().sync(getName(), listKey.toString(), () -> {
            List<Object> list = getList(listKey, false);
            return list.get(index);
        }, OpType.READ);
    }

    @Override
    public <T> T getFromList(Object listKey, int index, Class<T> clazz) {
        return syncCache().sync(getName(), listKey.toString(), () -> {
            Object value = getFromList(listKey, index);
            if (value != null && clazz != null && !clazz.isInstance(value)) {
                throw new IllegalStateException(
                        "Cached value is not of required type [" + clazz.getName() + "]: " + value);
            }
            return (T) value;
        }, OpType.READ);
    }

    @Override
    public void removeFromList(Object listKey, int index) {
        syncCache().sync(getName(), listKey.toString(), () -> {
            List<Object> list = getList(listKey, false);
            list.remove(index);
            return null;
        }, OpType.DELETE);
    }

    @Override
    public void addToSet(Object setKey, Object node) {
        syncCache().sync(getName(), setKey.toString(), () -> {
            Set<Object> set = getSet(setKey, true);
            set.add(node);
            return null;
        }, OpType.WRITE);
    }

    @Override
    public void removeFromSet(Object setKey, Object node) {
        syncCache().sync(getName(), setKey.toString(), () -> {
            Set<Object> set = getSet(setKey, false);
            set.remove(node);
            return null;
        }, OpType.DELETE);
    }

    @Override
    public synchronized boolean lock(Object lock) {
        Ehcache ehcache = getNativeCache();
        boolean isLock;
        try {
            isLock = ehcache.tryWriteLockOnKey(lock, 1);
        } catch (InterruptedException e) {
            isLock = false;
            Thread.currentThread().interrupt();
        }

        return isLock;
    }

    @Override
    public void unlock(Object lock) {
        Ehcache ehcache = getNativeCache();
        ehcache.releaseWriteLockOnKey(lock);
    }

    private static final AntPathMatcher ANT = new AntPathMatcher();

    @Override
    public List<String> keys(Object key) {
        return syncCache().sync(getName(), key.toString(), () -> {
            final String pattern = String.valueOf(key);
            Ehcache ehCache = getNativeCache();
            List<Object> keys = ehCache.getKeys();
            return keys.stream()
                    .map(String::valueOf)
                    .filter(b -> ANT.match(pattern, b))
                    .collect(Collectors.toList());
        }, OpType.READ);

    }
}
