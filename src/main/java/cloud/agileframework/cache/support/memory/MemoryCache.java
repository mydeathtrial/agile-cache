package cloud.agileframework.cache.support.memory;

import cloud.agileframework.cache.support.AgileCache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 佟盟
 * 日期 2020/7/17 10:40
 * 描述 内存缓存
 * @version 1.0
 * @since 1.0
 */
public class MemoryCache implements AgileCache {
    private final ConcurrentHashMap<Object, Node> store = new ConcurrentHashMap<>();
    private final String cacheName;

    public MemoryCache(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        return () -> {
            if (containKey(key)) {
                Node v = store.get(key);
                if (v != null) {
                    return v.getValue();
                }
            }
            return null;
        };
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (!containKey(key)) {
            put(key, value);
        }
        return get(key);
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        store.put(key, new Node(System.currentTimeMillis() + timeout.toMillis(), value));
    }

    @Override
    public void put(Object key, Object value) {
        store.put(key, new Node(-1, value));
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        return (T) (get(key).get());
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T v;
        try {
            v = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        this.put(key, v);
        return v;
    }

    @Override
    public void evict(Object key) {
        store.remove(key);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public boolean containKey(Object key) {
        Node node = store.get(key);
        if (node == null || (node.getTimeout() != -1 && node.getTimeout() <= System.currentTimeMillis())) {
            store.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {

        if (containKey(mapKey)) {
            Node node = store.get(mapKey);
            Object v = node.getValue();
            if (v == null || !Map.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是Map结构，无法存放缓存");
            }
            ((Map<Object, Object>) v).put(key, value);
        } else {
            Map<Object, Object> map = new HashMap<>(16);
            map.put(key, value);
            Node node = new Node(-1, map);
            store.put(mapKey, node);
        }
    }

    @Override
    public Object getFromMap(Object mapKey, Object key) {
        if (containKey(mapKey)) {
            Node node = store.get(mapKey);
            Object v = node.getValue();
            if (v == null || !Map.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是Map结构，无法获取缓存");
            }
            return ((Map<Object, Object>) v).get(key);
        }
        return null;
    }

    @Override
    public <T> T getFromMap(Object mapKey, Object key, Class<T> clazz) {
        Object v = getFromMap(mapKey, key);
        return (T) v;
    }

    @Override
    public void removeFromMap(Object mapKey, Object key) {
        if (containKey(mapKey)) {
            Node node = store.get(mapKey);
            Object v = node.getValue();
            if (v == null || !Map.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是Map结构，无法获取缓存");
            }
            ((Map<Object, Object>) v).remove(key);
        }
    }

    @Override
    public void addToList(Object listKey, Object value) {
        if (containKey(listKey)) {
            Node node = store.get(listKey);
            Object v = node.getValue();
            if (v == null || !List.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是List结构，无法存放缓存");
            }
            ((List<Object>) v).add(value);
        } else {
            List<Object> list = new ArrayList<>(16);
            list.add(value);
            Node node = new Node(-1, list);
            store.put(listKey, node);
        }
    }

    @Override
    public Object getFromList(Object listKey, int index) {
        if (containKey(listKey)) {
            Node node = store.get(listKey);
            Object v = node.getValue();
            if (v == null || !List.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是List结构，无法获取缓存");
            }
            return ((List<Object>) v).get(index);
        }
        return null;
    }

    @Override
    public <T> T getFromList(Object listKey, int index, Class<T> clazz) {
        Object v = getFromList(listKey, index);
        return (T) v;
    }

    @Override
    public void removeFromList(Object listKey, int index) {
        if (containKey(listKey)) {
            Node node = store.get(listKey);
            Object v = node.getValue();
            if (v == null || !List.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是List结构，无法获取缓存");
            }
            ((List<Object>) v).remove(index);
        }
    }

    @Override
    public void addToSet(Object setKey, Object value) {
        if (containKey(setKey)) {
            Node node = store.get(setKey);
            Object v = node.getValue();
            if (v == null || !Set.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是Set结构，无法存放缓存");
            }
            ((Set<Object>) v).add(value);
        } else {
            Set<Object> set = new HashSet<>(16);
            set.add(value);
            Node node = new Node(-1, set);
            store.put(setKey, node);
        }
    }

    @Override
    public void removeFromSet(Object setKey, Object value) {
        if (containKey(setKey)) {
            Node node = store.get(setKey);
            Object v = node.getValue();
            if (v == null || !Set.class.isAssignableFrom(v.getClass())) {
                throw new RuntimeException("目标缓存并不是Set结构，无法获取缓存");
            }
            ((Set<Object>) v).remove(node);
        }
    }

    @Override
    public boolean lock(Object lock) {
        return true;
    }

    @Override
    public boolean lock(Object lock, Duration timeout) {
        return true;
    }

    @Override
    public void unlock(Object lock) {
    }

    @Override
    public void unlock(Object lock, Duration timeout) {
    }
}
