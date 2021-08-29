package cloud.agileframework.cache.support.redis;

import cloud.agileframework.cache.support.AbstractAgileCache;
import cloud.agileframework.cache.util.BeanUtil;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2019/7/23 18:36
 * 描述 Redis缓存扩展
 * @version 1.0
 * @since 1.0
 */
public class AgileRedis extends AbstractAgileCache {
    private final Logger logger = LoggerFactory.getLogger(AgileRedis.class);

    private static final byte[] BINARY_NULL_VALUE = RedisSerializer.java().serialize(NullValue.INSTANCE);

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisCacheConfiguration cacheConfig;
    private final ConversionService conversionService;
    private final String name;

    private static final Duration SLEEP_TIME = Duration.ZERO;

    private static final String TAG = UUID.randomUUID().toString();
    /**
     * 存储数据的序列化工具
     */
    private final SecondCacheSerializerProvider redisSerializer = BeanUtil.getApplicationContext().getBean(SecondCacheSerializerProvider.class);

    AgileRedis(RedisCache cache, RedisConnectionFactory redisConnectionFactory) {
        super(cache);
        this.name = cache.getName();
        this.redisConnectionFactory = redisConnectionFactory;
        this.cacheConfig = cache.getCacheConfiguration();
        this.conversionService = cacheConfig.getConversionService();
    }

    @Override
    public void put(Object key, Object value, Duration timeout) {
        logger.info("操作:存\n区域：{}\nkey值：{}\nvalue值:{}\n", cache.getName(), key, value);
        execute(name, connection -> connection.set(createAndConvertCacheKey(key), serializeCacheValue(value), Expiration.from(timeout), RedisStringCommands.SetOption.UPSERT));
    }

    @Override
    public boolean containKey(Object key) {
        return execute(name, connection -> connection.exists(createAndConvertCacheKey(key)));
    }

    @Override
    public void addToMap(Object mapKey, Object key, Object value) {
        logger.info("操作:存\n区域：{}\nMap：{}\nkey值：{}\nvalue值:{}\n", cache.getName(), mapKey, key, value);
        executeConsumer(name, connection -> connection.hSet(createAndConvertCacheKey(mapKey), serializeCacheValue(key), serializeCacheValue(value)));
    }

    @Override
    public Object getFromMap(Object mapKey, Object key) {
        List<byte[]> list = execute(name, connection -> connection.hMGet(createAndConvertCacheKey(mapKey), serializeCacheValue(key)));
        if (list == null || list.get(0) == null) {
            return null;
        }

        return deserializeCacheValue(list.get(0));
    }

    @Override
    public <T> T getFromMap(Object mapKey, Object key, Class<T> clazz) {
        Object value = getFromMap(mapKey, key);

        return ObjectUtil.to(value, new TypeReference<>(clazz));
    }

    @Override
    public void removeFromMap(Object mapKey, Object key) {
        logger.info("操作:删\n区域：{}\nMap：{}\nkey值：{}\n", cache.getName(), mapKey, key);
        executeConsumer(name, connection -> connection.hDel(createAndConvertCacheKey(mapKey), serializeCacheValue(key)));
    }

    @Override
    public void addToList(Object listKey, Object node) {
        logger.info("操作:存\n区域：{}\nList：{}\nvalue值:{}\n", cache.getName(), listKey, node);
        executeConsumer(name, connection -> connection.rPush(createAndConvertCacheKey(listKey), serializeCacheValue(node)));
    }

    @Override
    public Object getFromList(Object listKey, int index) {
        List<byte[]> list = execute(name, connection -> connection.lRange(createAndConvertCacheKey(listKey), index, index + 1));
        if (list == null) {
            return null;
        }
        return deserializeCacheValue(list.get(0));
    }

    @Override
    public <T> T getFromList(Object listKey, int index, Class<T> clazz) {
        Object value = getFromList(listKey, index);
        return ObjectUtil.to(value, new TypeReference<>(clazz));
    }

    @Override
    public void removeFromList(Object listKey, int index) {
        logger.info("操作:删\n区域：{}\nList：{}\nvalue值:{}\n", cache.getName(), listKey, index);
        executeConsumer(name, connection -> connection.lRem(createAndConvertCacheKey(listKey), 1, serializeCacheValue(getFromList(listKey, index))));
    }

    @Override
    public void addToSet(Object setKey, Object node) {
        logger.info("操作:存\n区域：{}\nSet：{}\nvalue值:{}\n", cache.getName(), setKey, node);
        executeConsumer(name, connection -> connection.sAdd(createAndConvertCacheKey(setKey), serializeCacheValue(node)));
    }

    @Override
    public void removeFromSet(Object setKey, Object node) {
        logger.info("操作:删\n区域：{}\nSet：{}\nvalue值:{}\n", cache.getName(), setKey, node);
        executeConsumer(name, connection -> connection.sRem(createAndConvertCacheKey(setKey), serializeCacheValue(node)));
    }

    public synchronized boolean lock(Object lock, Object value) {
        return execute(name, connection -> connection.setNX(createAndConvertCacheKey(createCacheLockKey(lock)),
                serializeCacheValue(value)));
    }

    public synchronized boolean lock(Object lock, Object value, Duration timeout) {
        return execute(name, connection -> connection.set(createAndConvertCacheKey(createCacheLockKey(lock)),
                serializeCacheValue(value),
                Expiration.from(timeout),
                RedisStringCommands.SetOption.SET_IF_ABSENT));
    }

    /**
     * 同进程下的同线程，不重复加锁
     *
     * @param lock 锁
     * @return 是否成功上锁
     */
    public synchronized boolean lockOnThreadLocal(Object lock) {
        String value = getLock(lock, String.class);
        if (value != null && value.equals(getCurrentProcessAndThreadInfo())) {
            return true;
        } else if (value == null) {
            return lock(lock, getCurrentProcessAndThreadInfo());
        } else {
            return false;
        }
    }

    /**
     * 同进程下的同线程，更改过期时间
     *
     * @param lock    锁
     * @param timeout 过期
     * @return 是否成功上锁
     */
    public synchronized boolean lockOnThreadLocal(Object lock, Duration timeout) {
        String value = getLock(lock, String.class);
        if (value != null && value.equals(getCurrentProcessAndThreadInfo())) {
            execute(name, connection -> connection.expire(createAndConvertCacheKey(createCacheLockKey(lock)), timeout.getSeconds()));
            return true;
        } else if (value == null) {
            return lock(lock, getCurrentProcessAndThreadInfo(), timeout);
        } else {
            return false;
        }
    }

    /**
     * 取当前进程信息
     *
     * @return 当前进程+线程信息
     */
    private static String getCurrentProcessAndThreadInfo() {
        return TAG + Thread.currentThread().toString();
    }

    @Override
    public synchronized boolean lock(Object lock) {
        return lock(lock, new byte[0]);
    }

    @Override
    public synchronized void unlock(Object lock) {
        executeLockFree(connection -> connection.del(createAndConvertCacheKey(createCacheLockKey(lock))));
    }

    /**
     * 判断是否已经锁定
     *
     * @param lock 锁
     * @return 如果是重入，则返回false，如果没有锁，也返回false，意味可以对资源加锁
     */
    public synchronized boolean containLock(Object lock) {
        String value = getLock(lock, String.class);
        if (getCurrentProcessAndThreadInfo().equals(value) || value == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> keys(Object key) {
        Set<byte[]> set = execute(name, connection -> connection.keys(createAndConvertCacheKey(key)));
        return set.stream().map(n -> ((String) deserializeCacheKey(n)).replace(name + "::", "")).collect(Collectors.toList());
    }

    private byte[] createAndConvertCacheKey(Object key) {
        return serializeCacheKey(createCacheKey(key));
    }

    private byte[] serializeCacheKey(String cacheKey) {
        return ByteUtils.getBytes(cacheConfig.getKeySerializationPair().write(cacheKey));
    }

    private String createCacheKey(Object key) {

        String convertedKey = convertKey(key);

        if (!cacheConfig.usePrefix()) {
            return convertedKey;
        }

        return prefixCacheKey(convertedKey);
    }

    private String prefixCacheKey(String key) {

        // allow contextual cache names by computing the key prefix on every call.
        return cacheConfig.getKeyPrefixFor(name) + key;
    }


    private String convertKey(Object key) {

        TypeDescriptor source = TypeDescriptor.forObject(key);
        if (conversionService.canConvert(source, TypeDescriptor.valueOf(String.class))) {
            return conversionService.convert(key, String.class);
        }

        Method toString = ReflectionUtils.findMethod(key.getClass(), "toString");

        if (toString != null && !Object.class.equals(toString.getDeclaringClass())) {
            return key.toString();
        }

        throw new IllegalStateException(
                String.format("Cannot convert %s to String. Register a Converter or override toString().", source));
    }

    private byte[] serializeCacheValue(Object value) {

        if (isAllowNullValues() && value instanceof NullValue) {
            return BINARY_NULL_VALUE;
        }

        return redisSerializer.serialize(value);
    }

    protected Object deserializeCacheValue(byte[] value) {

        if (isAllowNullValues() && ObjectUtils.nullSafeEquals(value, BINARY_NULL_VALUE)) {
            return NullValue.INSTANCE;
        }

        return redisSerializer.deserialize(value);
    }

    protected Object deserializeCacheKey(byte[] value) {

        if (isAllowNullValues() && ObjectUtils.nullSafeEquals(value, BINARY_NULL_VALUE)) {
            return NullValue.INSTANCE;
        }

        return cacheConfig.getKeySerializationPair().read(ByteBuffer.wrap(value));
    }

    private boolean isAllowNullValues() {
        return ((RedisCache) cache).isAllowNullValues();
    }

    private <T> T execute(String name, Function<RedisConnection, T> callback) {

        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            checkAndPotentiallyWaitUntilUnlocked(name, connection);
            return callback.apply(connection);
        } finally {
            connection.close();
        }
    }

    private void executeConsumer(String name, Consumer<RedisConnection> callback) {

        RedisConnection connection = redisConnectionFactory.getConnection();
        try {

            checkAndPotentiallyWaitUntilUnlocked(name, connection);
            callback.accept(connection);
        } finally {
            connection.close();
        }
    }

    private void checkAndPotentiallyWaitUntilUnlocked(String name, RedisConnection connection) {

        if (!isLockingCacheWriter()) {
            return;
        }

        try {

            while (doCheckLock(name, connection)) {
                Thread.sleep(SLEEP_TIME.toMillis());
            }
        } catch (InterruptedException ex) {

            // Re-interrupt current thread, to allow other participants to react.
            Thread.currentThread().interrupt();

            throw new PessimisticLockingFailureException(String.format("Interrupted while waiting to unlock cache %s", name),
                    ex);
        }
    }

    private boolean isLockingCacheWriter() {
        return !SLEEP_TIME.isZero() && !SLEEP_TIME.isNegative();
    }

    private boolean doCheckLock(Object name, RedisConnection connection) {
        return connection.exists(createAndConvertCacheKey(createCacheLockKey(name)));
    }

    private static byte[] toBytes(String name) {
        return name.getBytes(StandardCharsets.UTF_8);
    }

    private static String createCacheLockKey(Object name) {
        return name.toString() + "~lock";
    }

    public <T> T getLock(Object lock, Class<T> clazz) {
        byte[] v = execute(name, connection -> connection.get(createAndConvertCacheKey(createCacheLockKey(lock))));
        return ObjectUtil.to(deserializeCacheValue(v), new TypeReference<>(clazz));
    }

    private void executeLockFree(Consumer<RedisConnection> callback) {

        RedisConnection connection = redisConnectionFactory.getConnection();

        try {
            callback.accept(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * 忽略集合处理直接存储
     *
     * @param key   key
     * @param value 值
     */
    public void putIgnoreAggregate(Object key, Object value) {
        execute(name, connection -> connection.set(createAndConvertCacheKey(key), serializeCacheValue(value)));
    }

    /**
     * 忽略集合处理直接存储
     *
     * @param key     key
     * @param value   值
     * @param timeout 过期
     */
    public void putIgnoreAggregate(Object key, Object value, Duration timeout) {
        execute(name, connection -> connection.set(createAndConvertCacheKey(key), serializeCacheValue(value), Expiration.from(timeout), RedisStringCommands.SetOption.UPSERT));
    }

    @Override
    public void put(Object key, Object value) {
        logger.info("操作:存\n区域：{}\nkey值：{}\nvalue值：{}\n", cache.getName(), key, value);
        if (Map.class.isAssignableFrom(value.getClass())) {
            evict(key);
            Map<byte[], byte[]> map = new HashMap<>(((Map<?, ?>) value).size());
            ((Map<?, ?>) value).forEach((eKey, eValue) -> map.put(serializeCacheValue(eKey), serializeCacheValue(eValue)));
            try {
                executeConsumer(name, connection -> connection.hMSet(createAndConvertCacheKey(key), map));
            } catch (RedisSystemException e) {
                map.forEach((eKey, eValue) -> {
                    addToMap(key, eKey, eValue);
                });
            }

        } else if (List.class.isAssignableFrom(value.getClass())) {
            evict(key);
            int size = ((List<?>) value).size();
            byte[][] arr = new byte[size][];
            List<byte[]> result = ((List<?>) value).stream().map(this::serializeCacheValue).collect(Collectors.toList());
            byte[][] list = result.toArray(arr);
            execute(name, connection -> connection.rPush(createAndConvertCacheKey(key), list));
        } else if (Set.class.isAssignableFrom(value.getClass())) {
            evict(key);
            int size = ((Set<?>) value).size();
            byte[][] arr = new byte[size][];
            Set<byte[]> result = ((Set<?>) value).stream().map(this::serializeCacheValue).collect(Collectors.toSet());
            byte[][] set = result.toArray(arr);
            execute(name, connection -> connection.sAdd(createAndConvertCacheKey(key), set));
        } else {
            putIgnoreAggregate(key, value);
        }
    }

    @Override
    public <T> T get(Object key, Class<T> clazz) {
        return get(key, new TypeReference<>(clazz));
    }

    @Override
    public <T> T get(Object key, TypeReference<T> typeReference) {
        logger.info("操作:取\n区域：{}\nkey值：{}\n", cache.getName(), key);
        Class<?> clazz = ClassUtil.getWrapper(typeReference.getType());

        Object data;
        if (Map.class.isAssignableFrom(clazz)) {
            Map<byte[], byte[]> map = execute(name, connection -> connection.hGetAll(createAndConvertCacheKey(key)));
            HashMap<Object, Object> res = new HashMap<>(map.size());
            map.forEach((k, v) -> res.put(deserializeCacheValue(k), deserializeCacheValue(v)));
            data = res;
        } else if (List.class.isAssignableFrom(clazz)) {
            List<byte[]> list = execute(name, connection -> connection.lRange(createAndConvertCacheKey(key), 0, -1));
            data = list.stream().map(this::deserializeCacheValue).collect(Collectors.toList());
        } else if (Set.class.isAssignableFrom(clazz)) {
            Set<byte[]> set = execute(name, connection -> connection.sMembers(createAndConvertCacheKey(key)));
            data = set.stream().map(this::deserializeCacheValue).collect(Collectors.toSet());
        } else {
            byte[] v = execute(name, connection -> connection.get(createAndConvertCacheKey(key)));
            data = deserializeCacheValue(v);
        }
        return ObjectUtil.to(data, new TypeReference<>(clazz));
    }

    @Override
    public ValueWrapper get(Object key) {
        byte[] v = execute(name, connection -> connection.get(createAndConvertCacheKey(key)));
        return new SimpleValueWrapper(deserializeCacheValue(v));
    }

    /**
     * 获取key的过期时间
     *
     * @param key key
     * @return 秒
     */
    public Long getTimeout(Object key) {
        return execute(name, connection -> connection.ttl(createAndConvertCacheKey(key)));
    }

    @Override
    public RedisConnection getNativeCache() {
        return redisConnectionFactory.getConnection();
    }


}
