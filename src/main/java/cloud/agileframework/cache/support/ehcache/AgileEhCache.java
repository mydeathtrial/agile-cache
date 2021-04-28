package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AbstractAgileCache;
import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.sync.SyncKeys;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.spring.util.BeanUtil;
import com.google.common.collect.Maps;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.NumberUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2019/7/23 18:17
 * 描述 Ehcache实现
 * @version 1.0
 * @since 1.0
 */
public class AgileEhCache extends AbstractAgileCache {

    private final Ehcache nativeCache;

    AgileEhCache(EhCacheCache cache) {
        super(cache);
        nativeCache = cache.getNativeCache();
    }

    @Override
    public Ehcache getNativeCache() {
        return nativeCache;
    }

    private SyncCache syncCache() {
        return BeanUtil.getBean(SyncCache.class);
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            ValueWrapper v = super.get(key);
            if (v == null) {
                return null;
            }
            final Object o = v.get();

            if (o == null) {
                return null;
            }

            return ObjectUtil.to(o, new TypeReference<>(clazz));
        }, OpType.READ);
    }

    @Override
    public ValueWrapper get(Object key) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> super.get(key), OpType.READ);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> super.get(key, valueLoader), OpType.READ);
    }

    @Override
    public <T> T get(Object key, TypeReference<T> typeReference) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            ValueWrapper wrapper = super.get(key);
            if (wrapper != null) {
                Object v = wrapper.get();
                return ObjectUtil.to(v, typeReference);
            }
            return null;
        }, OpType.READ);
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        directPut(key, value, timeout);
    }

    @Override
    public void put(Object key, Object value) {
        directPut(key, value);
    }

    @Override
    public boolean containKey(Object key) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> nativeCache.isKeyInCache(key), OpType.READ);
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {
        Map<Object, Object> map = directGetMap(mapKey);
        map.put(key, value);
        directPut(mapKey, map);
    }

    @Override
    public Object getFromMap(Object mapKey, Object key) {
        return syncCache().sync(SyncKeys.of(getName(), mapKey), () -> {
            Map<Object, Object> map = directGetMap(mapKey);
            return map.get(key);
        }, OpType.READ);
    }

    @Override
    public <T> T getFromMap(Object mapKey, Object key, Class<T> clazz) {
        return syncCache().sync(SyncKeys.of(getName(), mapKey), () -> {
            Map<Object, Object> map = directGetMap(mapKey);
            Object value = map.get(key);
            return ObjectUtil.to(value, new TypeReference<>(clazz));
        }, OpType.READ);
    }

    @Override
    public void removeFromMap(Object mapKey, Object key) {
        Map<Object, Object> map = directGetMap(mapKey);
        if (map.containsKey(key)) {
            map.remove(key);
            directPut(mapKey, map);
        }
    }

    @Override
    public void addToList(Object listKey, Object node) {
        List<Object> list = directGetList(listKey);
        list.add(node);
        directPut(listKey, list);
    }

    @Override
    public Object getFromList(Object listKey, int index) {
        return syncCache().sync(SyncKeys.of(getName(), listKey), () -> {
            List<Object> list = directGetList(listKey);
            if (list.size() >= index) {
                return list.get(index);
            }
            return null;
        }, OpType.READ);
    }

    @Override
    public <T> T getFromList(Object listKey, int index, Class<T> clazz) {
        return syncCache().sync(SyncKeys.of(getName(), listKey), () -> {
            List<Object> list = directGetList(listKey);
            Object value = null;
            if (list.size() >= index) {
                value = list.get(index);
            }

            return ObjectUtil.to(value, new TypeReference<>(clazz));
        }, OpType.READ);
    }

    @Override
    public void removeFromList(Object listKey, int index) {
        List<Object> list = directGetList(listKey);
        if (list.size() >= index) {
            list.remove(index);
            directPut(listKey, list);
        }
    }

    @Override
    public void addToSet(Object setKey, Object node) {
        Set<Object> set = directGetSet(setKey);
        set.add(node);
        directPut(setKey, set);
    }

    @Override
    public void removeFromSet(Object setKey, Object node) {
        Set<Object> set = directGetSet(setKey);
        if (set.remove(node)) {
            directPut(setKey, set);
        }
    }

    @Override
    public synchronized boolean lock(Object lock) {
        boolean isLock;
        try {
            isLock = nativeCache.tryWriteLockOnKey(lock, 1);
        } catch (InterruptedException e) {
            isLock = false;
            Thread.currentThread().interrupt();
        }

        return isLock;
    }

    @Override
    public void unlock(Object lock) {
        nativeCache.releaseWriteLockOnKey(lock);
    }

    private static final AntPathMatcher ANT = new AntPathMatcher();

    @Override
    public List<String> keys(Object key) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            final String pattern = String.valueOf(key);
            List<Object> keys = nativeCache.getKeys();
            return keys.stream()
                    .map(String::valueOf)
                    .filter(b -> ANT.match(pattern, b))
                    .collect(Collectors.toList());
        }, OpType.READ);

    }

    public void directPut(Object key, Object value, Duration timeout) {
        Element element = new Element(key, SerializationUtils.clone((Serializable) value));
        element.setTimeToLive(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
        element.setTimeToIdle(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
        element.setEternal(false);

        Element old = nativeCache.get(key);
        if (old == null) {
            nativeCache.put(element);
        } else {
            nativeCache.replace(old, element);
        }
    }

    public void directPut(Object key, Object value) {
        //内存缓存保留28~32分钟之间的随机值，防止穿透，
        long randomRange = RandomUtils.nextLong(1680000, 1920000);
        final Duration timeout = Duration.ofMillis(randomRange);
        Ehcache ehCache = nativeCache;
        Element element = new Element(key, SerializationUtils.clone((Serializable) value));
        element.setTimeToIdle(NumberUtils.parseNumber(Long.toString(timeout.getSeconds()), Integer.class));
        element.setEternal(false);

        Element old = ehCache.get(key);
        if (old == null) {
            ehCache.put(element);
        } else {
            ehCache.replace(old, element);
        }
    }

    public void directEvict(Object key) {
        super.evict(key);
    }

    private Map<Object, Object> directGetMap(Object mapKey) {
        Element value = nativeCache.get(mapKey);

        if (value == null) {
            return Maps.newHashMapWithExpectedSize(16);
        }
        Object map = value.getObjectValue();
        if (!Map.class.isAssignableFrom(map.getClass())) {
            throw CACHE_EXCEPTION;
        }

        return (Map<Object, Object>) map;
    }

    private List<Object> directGetList(Object listKey) {
        Element value = nativeCache.get(listKey);
        if (value == null) {
            return Collections.emptyList();
        }
        Object list = value.getObjectValue();
        if (!List.class.isAssignableFrom(list.getClass())) {
            throw CACHE_EXCEPTION;
        }
        return (List) list;
    }

    private Set<Object> directGetSet(Object setKey) {
        Element value = nativeCache.get(setKey);
        if (value == null) {
            return Collections.emptySet();
        }
        Object map = value.getObjectValue();
        if (!Set.class.isAssignableFrom(map.getClass())) {
            throw CACHE_EXCEPTION;
        }
        return (Set) map;
    }
}
