package cloud.agileframework.cache.util;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.support.AgileCacheManagerInterface;
import cloud.agileframework.spring.util.BeanUtil;
import org.springframework.cache.Cache;

import java.time.Duration;

/**
 * @author 佟盟
 * 日期 2019/4/19 17:35
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class CacheUtil {
    private static final String DEFAULT_CACHE_NAME = "common-cache";

    public static AgileCacheManagerInterface getAgileCacheManager() {
        return BeanUtil.getBean(AgileCacheManagerInterface.class);
    }

    public static AgileCache getCache() {
        return getAgileCacheManager().getCache(DEFAULT_CACHE_NAME);
    }

    public static AgileCache getCache(String cacheName) {
        return getAgileCacheManager().getCache(cacheName);
    }

    public static void put(Object key, Object value, int timeout) {
        getCache(DEFAULT_CACHE_NAME).put(key, value, Duration.ofSeconds(timeout));
    }

    /**
     * 获取缓存区域名
     *
     * @return 例如:common-cache
     */
    public static String getName() {
        return getCache(DEFAULT_CACHE_NAME).getName();
    }

    /**
     * 直接获取缓存客户端
     *
     * @return 例如:RedisConnection/Ehcache
     */
    public static Object getNativeCache() {
        return getCache(DEFAULT_CACHE_NAME).getNativeCache();
    }

    /**
     * 直接取值
     *
     * @param key 索引
     * @return 值
     */
    public static Object get(Object key) {
        Cache.ValueWrapper v = getCache(DEFAULT_CACHE_NAME).get(key);
        if (v == null) {
            return null;
        }
        return v.get();
    }

    /**
     * 如果不存在就存，存在就不存
     *
     * @param key   索引
     * @param value 值
     */
    public static void putIfAbsent(String key, Object value) {
        getCache(DEFAULT_CACHE_NAME).putIfAbsent(key, value);
    }


    /**
     * 过期存储
     *
     * @param key     索引
     * @param value   值
     * @param timeout 过期时间
     */
    public static void put(Object key, Object value, Duration timeout) {
        getCache(DEFAULT_CACHE_NAME).put(key, value, timeout);
    }

    /**
     * 存储
     *
     * @param key   索引
     * @param value 值
     */

    public static void put(Object key, Object value) {
        getCache(DEFAULT_CACHE_NAME).put(key, value);
    }

    /**
     * 获取
     *
     * @param key   索引
     * @param clazz 类型
     * @param <T>   泛型
     * @return 值
     */

    public static <T> T get(Object key, Class<T> clazz) {
        return getCache(DEFAULT_CACHE_NAME).get(key, clazz);
    }

    /**
     * 删除
     *
     * @param key 索引
     */

    public static void evict(Object key) {
        getCache(DEFAULT_CACHE_NAME).evict(key);
    }

    /**
     * 清理
     */

    public static void clear() {
        getCache(DEFAULT_CACHE_NAME).clear();
    }

    /**
     * 判断
     *
     * @param key 索引
     * @return 是否
     */
    public static boolean containKey(Object key) {
        return getCache(DEFAULT_CACHE_NAME).containKey(key);
    }

    /**
     * 向Map中添加数据
     *
     * @param mapKey map索引
     * @param key    key
     * @param value  value
     */
    public static void addToMap(Object mapKey, Object key, Object value) {
        getCache(DEFAULT_CACHE_NAME).addToMap(mapKey, key, value);
    }

    /**
     * 查询Map中索引key对应的value数据
     *
     * @param mapKey map索引
     * @param key    key
     * @return 值
     */
    public static Object getFromMap(Object mapKey, Object key) {
        return getCache(DEFAULT_CACHE_NAME).getFromMap(mapKey, key);
    }

    /**
     * 查询Map中索引key对应的value数据
     *
     * @param mapKey map索引
     * @param key    key
     * @return 值
     */
    public static <T> T getFromMap(Object mapKey, Object key, Class<T> tClass) {
        return getCache(DEFAULT_CACHE_NAME).getFromMap(mapKey, key, tClass);
    }

    /**
     * 从Map中删除索引key
     *
     * @param mapKey map索引
     * @param key    key
     */
    public static void removeFromMap(Object mapKey, Object key) {
        getCache(DEFAULT_CACHE_NAME).removeFromMap(mapKey, key);
    }

    /**
     * 向List中添加节点
     *
     * @param listKey list索引
     * @param node    节点数据
     */
    public static void addToList(Object listKey, Object node) {
        getCache(DEFAULT_CACHE_NAME).addToList(listKey, node);
    }

    /**
     * 从List中获取下标为index下的节点数据
     *
     * @param listKey list索引
     * @param index   节点下标
     * @return 值
     */
    public static Object getFromList(Object listKey, int index) {
        return getCache(DEFAULT_CACHE_NAME).getFromList(listKey, index);
    }

    /**
     * 从List中获取下标为index下的节点数据
     *
     * @param listKey list索引
     * @param index   节点下标
     * @return 值
     */
    public static <T> T getFromList(Object listKey, int index, Class<T> tClass) {
        return getCache(DEFAULT_CACHE_NAME).getFromList(listKey, index, tClass);
    }

    /**
     * 从List中删除下标节点
     *
     * @param listKey list索引
     * @param index   节点下标
     */
    public static void removeFromList(Object listKey, int index) {
        getCache(DEFAULT_CACHE_NAME).removeFromList(listKey, index);
    }

    /**
     * 向set中添加节点
     *
     * @param setKey set索引
     * @param node   节点数据
     */
    public static void addToSet(Object setKey, Object node) {
        getCache(DEFAULT_CACHE_NAME).addToSet(setKey, node);
    }

    /**
     * set中删除节点
     *
     * @param setKey set索引
     * @param node   节点数据
     */
    public static void removeFromSet(Object setKey, Object node) {
        getCache(DEFAULT_CACHE_NAME).removeFromSet(setKey, node);
    }

    /**
     * 分布式同步锁
     *
     * @param lock 锁标识
     * @return 是否加锁成功
     */
    public static boolean lock(Object lock) {
        return getCache(DEFAULT_CACHE_NAME).lock(lock);
    }

    /**
     * 分布式同步锁
     *
     * @param lock    锁标识
     * @param timeout 超时
     * @return 是否加锁成功
     */
    public static boolean lock(Object lock, Duration timeout) {
        return getCache(DEFAULT_CACHE_NAME).lock(lock, timeout);
    }

    /**
     * 解锁
     *
     * @param lock 锁标识
     */
    public static void unlock(Object lock) {
        getCache(DEFAULT_CACHE_NAME).unlock(lock);
    }

    /**
     * 解锁
     *
     * @param lock 锁标识
     */
    public static void unlock(Object lock, Duration timeout) {
        getCache(DEFAULT_CACHE_NAME).unlock(lock, timeout);
    }
}
