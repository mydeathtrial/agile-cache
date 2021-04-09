package cloud.agileframework.cache.sync;

import cloud.agileframework.cache.support.AgileCache;
import cloud.agileframework.cache.support.ehcache.AgileEhCache;
import cloud.agileframework.cache.support.ehcache.AgileEhCacheCacheManager;
import cloud.agileframework.cache.support.redis.AgileRedis;
import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.spring.util.AsyncUtil;
import lombok.SneakyThrows;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author 佟盟
 * 日期 2021-03-15 11:41
 * 描述 ehcache与redis缓存同步
 * @version 1.0
 * @since 1.0
 */
public class RedisSyncCache extends AbstractSyncCache implements MessageListener {
    public static final String LOCK_CACHE_KEY = "key";

    @Autowired
    private AgileRedisCacheManager agileRedisCacheManager;

    @Autowired
    private AgileEhCacheCacheManager agileEhCacheCacheManager;

    /**
     * redis操作工具，用于广播
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 通知发布
     *
     * @param channel         渠道
     * @param newCacheVersion 新版本
     */
    private void notice(String channel, int newCacheVersion) {
        //发布
        redisTemplate.convertAndSend(channel, Integer.toString(newCacheVersion));
    }

    /**
     * 订阅
     *
     * @param message 消息
     * @param pattern 与通道匹配的模式（如果指定）-可以为空。
     */
    @SneakyThrows
    @Override
    public void onMessage(Message message, byte[] pattern) {
        Object channel = redisTemplate.getKeySerializer().deserialize(message.getChannel());

        //提取通知的最新版本号
        Object noticeVersionData = redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (noticeVersionData instanceof String && NumberUtils.isCreatable((String) noticeVersionData)) {

            //按照缓存的版本号进行同步
            message((String) channel, NumberUtils.toInt((String) noticeVersionData));
            return;
        }
        //接收到非数字，不做同步
        throw new CacheSyncException("Notification content does not conform to version number format");
    }


    /**
     * ehcache到redis
     *
     * @param syncKeys key信息
     */
    private void ehcacheToRedis(SyncKeys syncKeys, OpType opType) {

        final AgileRedis redisCache = agileRedisCacheManager.getCache(syncKeys.getRegion());

        if (OpType.DELETE == opType) {
            syncKeys.getVersionData().set(-1);
            redisCache.evict(syncKeys.getData());
            redisCache.evict(syncKeys.getVersion());
        } else if (OpType.WRITE == opType) {
            Element element = agileEhCacheCacheManager.getCache(syncKeys.getRegion()).getNativeCache().get(syncKeys.getData());
            if (element == null || element.getObjectValue() == null) {
                return;
            }

            //升级版本号
            syncKeys.getVersionData().addAndGet(1);
            redisCache.putIgnoreAggregate(syncKeys.getData(), element.getObjectValue());
            redisCache.putIgnoreAggregate(syncKeys.getVersion(), syncKeys.getVersionData().get());
        }
    }

    /**
     * redis到ehcache
     * a
     *
     * @param syncKeys key信息
     */
    private void redisToEhcache(SyncKeys syncKeys, OpType opType) {
        final AgileEhCache ehcache = agileEhCacheCacheManager.getCache(syncKeys.getRegion());
        if (OpType.DELETE == opType) {
            ehcache.directEvict(syncKeys.getData());
        } else if (OpType.READ == opType || OpType.WRITE == opType) {
            //取缓存数据
            final AgileCache redisCache = agileRedisCacheManager.getCache(syncKeys.getRegion());
            Cache.ValueWrapper valueWrapper = redisCache.get(syncKeys.getData());

            if (valueWrapper == null || valueWrapper.get() == null) {
                return;
            }


            ehcache.directPut(syncKeys.getData(), valueWrapper.get());

            syncVersion(syncKeys);
        }
    }

    /**
     * 同步版本号
     *
     * @param syncKeys key信息
     */
    private void syncVersion(SyncKeys syncKeys) {
        final AgileRedis redisCache = agileRedisCacheManager.getCache(syncKeys.getRegion());
        //同步版本号
        final Integer cacheVersion = redisCache.get(syncKeys.getVersion(), Integer.class);
        if (cacheVersion != null) {
            syncKeys.getVersionData().set(cacheVersion);
        }
    }

    @SneakyThrows
    public void message(String channel, int noticeVersion) {
        //取缓存数据
        final AgileCache redisCache = agileRedisCacheManager.getCache(channel);

        //提取缓存中的版本号
        final SyncKeys syncKeys = keysByChannel(channel);
        final Integer cacheVersionData = redisCache.get(syncKeys.getVersion(), Integer.class);
        if (cacheVersionData == null) {
            //缓存中如果没有版本号，说明系统缓存数据被误删，不同步
            throw new CacheSyncException("The cached version number was not found");
        }


        int cacheVersion = NumberUtils.toInt(cacheVersionData.toString());
        if (-1 == noticeVersion) {
            redisToEhcache(syncKeys, OpType.DELETE);
        } else if (cacheVersion < noticeVersion) {
            //缓存的版本号小于通知的版本号，说明出现缓存数据同步错误，理论上缓存版本号只可能大于通知的版本号
            throw new CacheSyncException("The version number of the notification does not match the version number of the cache");
        } else {
            redisToEhcache(syncKeys, OpType.WRITE);
        }
    }

    /**
     * 并发锁
     *
     * @param syncKeys key信息
     * @return true取锁成功，false失败
     */
    private boolean writeLock(SyncKeys syncKeys) {
        final AgileRedis redisCache = agileRedisCacheManager.getCache(LOCK_CACHE_KEY);
        if (redisCache.containLock(syncKeys.getReadLock())) {
            return false;
        }
        return redisCache.lockOnThreadLocal(syncKeys.getWriteLock(), Duration.ofSeconds(120));
    }


    /**
     * 并发锁
     *
     * @param syncKeys key信息
     * @return true取锁成功，false失败
     */
    private boolean readLock(SyncKeys syncKeys) {
        final AgileRedis redisCache = agileRedisCacheManager.getCache(LOCK_CACHE_KEY);
        if (redisCache.containLock(syncKeys.getWriteLock())) {
            return true;
        }
        return redisCache.lockOnThreadLocal(syncKeys.getReadLock(), Duration.ofSeconds(120));
    }

    /**
     * 解锁
     *
     * @param lockKey 锁
     */
    private void unlock(String lockKey) {
        final AgileCache redisCache = agileRedisCacheManager.getCache(LOCK_CACHE_KEY);
        redisCache.unlock(lockKey);
    }

    @Override
    public <T> T sync(String region, String key, Supplier<T> supplier, OpType opType) {
        T result = null;
        SyncKeys syncKeys = keys(region, key);

        //自旋10次
        int count = 1200;

        while (count > 0) {

            switch (opType) {
                case READ:
                    if (syncData(syncKeys)) {
                        return supplier.get();
                    }
                    break;
                case WRITE:
                case DELETE:
                    if (writeLock(syncKeys)) {
                        try {
                            //检查乐观锁
                            checkCAS(syncKeys);
                            //某操作
                            result = supplier.get();
                            //异步执行
                            AsyncUtil.execute(() -> ehcacheToRedisAndNotice(syncKeys, opType));
                            return result;
                        } catch (OptimisticLockCheckError e) {
                            unlock(syncKeys.getWriteLock());
                            throw e;
                        } catch (Exception e) {
                            e.printStackTrace();
                            unlock(syncKeys.getWriteLock());
                        }
                    }
                    break;
                default:
            }

            try {
                //间隔两秒自旋
                Thread.sleep(Duration.ofMillis(10).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            count--;
        }
        return result;
    }

    /**
     * 检查CAS情况
     *
     * @param syncKeys key信息
     */
    private void checkCAS(SyncKeys syncKeys) throws OptimisticLockCheckError {
        int ehcacheVersion = syncKeys.getVersionData().get();
        if (ehcacheVersion == 0) {
            syncVersion(syncKeys);
            ehcacheVersion = syncKeys.getVersionData().get();
        }

        final AgileRedis redisCache = agileRedisCacheManager.getCache(syncKeys.getRegion());
        final Integer redisVersion = redisCache.get(syncKeys.getVersion(), int.class);

        boolean noChange = redisVersion == null || redisVersion <= ehcacheVersion;
        if (!noChange) {
            throw new OptimisticLockCheckError("redis version is illegal");
        }
    }

    @Override
    public void clear(String region) {
        Ehcache ehcache = agileEhCacheCacheManager.getCache(region).getNativeCache();
        List keys = ehcache.getKeys();
        keys.forEach(key -> sync(region, key.toString(), () -> null, OpType.DELETE));
    }

    /**
     * ehcache缓存同步到redis并且通知共享缓存相关程序
     *
     * @param syncKeys 缓存同步相关key信息
     * @param opType   操作类型
     */
    private synchronized void ehcacheToRedisAndNotice(SyncKeys syncKeys, OpType opType) {

        //通知其他缓存共享程序同步
        try {
            //数据发生变化时，内存向缓存同步
            ehcacheToRedis(syncKeys, opType);

            //通知共享缓存程序缓存同步
            notice(syncKeys.getChannel(), syncKeys.getVersionData().get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unlock(syncKeys.getWriteLock());
        }
        if (OpType.DELETE == opType) {
            //清除删除的缓存
            remove(syncKeys);
        }
    }

    /**
     * 同步数据
     *
     * @param syncKeys 缓存同步相关key
     * @return 成功
     */
    private boolean syncData(SyncKeys syncKeys) {
        if (readLock(syncKeys)) {
            try {
                Element element = agileEhCacheCacheManager.getCache(syncKeys.getRegion()).getNativeCache().get(syncKeys.getData());
                if (element == null || element.getObjectValue() == null) {
                    redisToEhcache(syncKeys, OpType.READ);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                unlock(syncKeys.getReadLock());
            }
            return true;
        }
        return false;
    }
}
