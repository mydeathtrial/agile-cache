package cloud.agileframework.cache.support.ehcache;

import cloud.agileframework.cache.support.AbstractAgileCache;
import cloud.agileframework.cache.sync.OpType;
import cloud.agileframework.cache.sync.SyncCache;
import cloud.agileframework.cache.sync.SyncKeys;
import cloud.agileframework.cache.util.BeanUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
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

    private static final AntPathMatcher ANT = new AntPathMatcher();
    private final Ehcache nativeCache;

    AgileEhCache(EhCacheCache cache) {
        super(cache);
        nativeCache = cache.getNativeCache();
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return super.evictIfPresent(TransmitKey.of(key));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return super.putIfAbsent(TransmitKey.of(key), value);
    }

    @Override
    public void evict(Object key) {
        super.evict(TransmitKey.of(key));
    }

    @Override
    public Ehcache getNativeCache() {
        return nativeCache;
    }

    private SyncCache syncCache() {
        return BeanUtil.getApplicationContext().getBean(SyncCache.class);
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            ValueWrapper v = super.get(TransmitKey.of(key));
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
        return syncCache().sync(SyncKeys.of(getName(), key), () -> super.get(TransmitKey.of(key)), OpType.READ);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> super.get(TransmitKey.of(key), valueLoader), OpType.READ);
    }

    @Override
    public <T> T get(Object key, TypeReference<T> typeReference) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            ValueWrapper wrapper = super.get(TransmitKey.of(key));
            if (wrapper != null) {
                Object v = wrapper.get();
                return ObjectUtil.to(v, typeReference);
            }
            return null;
        }, OpType.READ);
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        directPut(TransmitKey.of(key), value, timeout);
    }

    @Override
    public void put(Object key, Object value) {
        directPut(TransmitKey.of(key), value);
    }

    @Override
    public boolean containKey(Object key) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> nativeCache.isKeyInCache(TransmitKey.of(key)), OpType.READ);
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {
        Map<Object, Object> map = directGetMap(mapKey);
        map.put(key, value);
        directPut(TransmitKey.of(mapKey), map);
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
            directPut(TransmitKey.of(mapKey), map);
        }
    }

    @Override
    public void addToList(Object listKey, Object node) {
        List<Object> list = directGetList(listKey);
        list.add(node);
        directPut(TransmitKey.of(listKey), list);
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
            directPut(TransmitKey.of(listKey), list);
        }
    }

    @Override
    public void addToSet(Object setKey, Object node) {
        Set<Object> set = directGetSet(setKey);
        set.add(node);
        directPut(TransmitKey.of(setKey), set);
    }

    @Override
    public void removeFromSet(Object setKey, Object node) {
        Set<Object> set = directGetSet(setKey);
        if (set.remove(node)) {
            directPut(TransmitKey.of(setKey), set);
        }
    }

    @Override
    public boolean connectKey(Object setKey, Object node) {
        Set<Object> set = directGetSet(setKey);
        return set.contains(node);
    }

    @Override
    public synchronized boolean lock(Object lock) {
        boolean isLock;
        try {
            isLock = nativeCache.tryWriteLockOnKey(TransmitKey.of(lock), 1);
        } catch (InterruptedException e) {
            isLock = false;
            Thread.currentThread().interrupt();
        }

        return isLock;
    }

    @Override
    public void unlock(Object lock) {
        nativeCache.releaseWriteLockOnKey(TransmitKey.of(lock));
    }

    @Override
    public List<String> keys(Object key) {
        return syncCache().sync(SyncKeys.of(getName(), key), () -> {
            final String pattern = String.valueOf(key);
            List<Object> keys = nativeCache.getKeys();
            return keys.stream()
                    .map(a -> {
                        if (a instanceof TransmitKey) {
                            return ObjectUtil.toString(((TransmitKey) a).getKey());
                        }
                        return ObjectUtil.toString(a);
                    })
                    .filter(b -> ANT.match(pattern, b))
                    .collect(Collectors.toList());
        }, OpType.READ);

    }

    public void directPut(Object key, Object value, Duration timeout) {
        if (!(key instanceof TransmitKey)) {
            key = TransmitKey.of(key, false);
        }
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
        if (!(key instanceof TransmitKey)) {
            key = TransmitKey.of(key, false);
        }
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
        if (!(key instanceof TransmitKey)) {
            key = TransmitKey.of(key, false);
        }
        super.evict(key);
    }

    private Map<Object, Object> directGetMap(Object mapKey) {
        Map<Object, Object> map = get(mapKey, Map.class);

        if (map == null) {
            return Maps.newHashMapWithExpectedSize(16);
        }

        return map;
    }

    private List<Object> directGetList(Object listKey) {
        List<Object> list = get(listKey, List.class);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    private Set<Object> directGetSet(Object setKey) {
        Set<Object> set = get(setKey, Set.class);
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }
}
