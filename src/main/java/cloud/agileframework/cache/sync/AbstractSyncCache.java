package cloud.agileframework.cache.sync;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * @author 佟盟
 * 日期 2021-03-22 11:23
 * 描述 抽象缓存同步
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSyncCache implements SyncCache {
    AbstractSyncCache() {
    }

    /**
     * 每个渠道与取到版本号缓存key映射
     */
    private static final Map<String, SyncKeys> KEY_INFO = Maps.newConcurrentMap();

    /**
     * 每个渠道与取到版本号缓存key映射
     */
    private static final Map<String, SyncKeys> CHANNEL_KEY_INFO = Maps.newConcurrentMap();

    /**
     * 初始化缓存同步相关的缓存key值
     *
     * @param key 缓存key
     */
    private static void initKeys(String region, String key) {
        SyncKeys syncKeys = SyncKeys.builder()
                .region(region)
                .channel(key + "_channel")
                .data(key)
                .version(key + "_version")
                .readLock(key + "_readLock")
                .writeLock(key + "_writeLock")
                .build();
        KEY_INFO.put(region + key, syncKeys);
        CHANNEL_KEY_INFO.put(region + key + "_channel", syncKeys);
    }

    /**
     * 根据缓存key生成对应的缓存同步所需的key值集合
     *
     * @param key 缓存key
     * @return 缓存同步所需的key值集合
     */
    public static synchronized SyncKeys keys(String region, String key) {
        SyncKeys syncKeys = KEY_INFO.get(region + key);
        if (syncKeys == null) {
            initKeys(region, key);
        }
        return KEY_INFO.get(region + key);
    }

    /**
     * 根据信道生成对应的缓存同步所需的key值集合
     *
     * @param channel 信道
     * @return 缓存同步所需的key值集合
     */
    @SneakyThrows
    static synchronized SyncKeys keysByChannel(String channel) {
        SyncKeys syncKeys = CHANNEL_KEY_INFO.get(channel);
        if (syncKeys == null) {
            throw new CacheSyncException("未找到缓存同步相关key信息");
        }
        return syncKeys;
    }

    static void remove(SyncKeys syncKeys) {
        KEY_INFO.remove(syncKeys.getRegion() + syncKeys.getData());
        CHANNEL_KEY_INFO.remove(syncKeys.getRegion() + syncKeys.getChannel());
    }
}
