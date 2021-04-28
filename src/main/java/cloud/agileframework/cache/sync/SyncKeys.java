package cloud.agileframework.cache.sync;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 佟盟
 * 日期 2021-03-15 11:42
 * 描述 缓存同步key信息集合
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
public class SyncKeys {

    /**
     * 每个渠道与取到版本号缓存key映射
     */
    private static final Map<String, SyncKeys> KEY_INFO = Maps.newConcurrentMap();

    /**
     * 每个渠道与取到版本号缓存key映射
     */
    private static final Map<String, SyncKeys> CHANNEL_KEY_INFO = Maps.newConcurrentMap();

    /**
     * 版本号
     */
    private final AtomicInteger versionData = new AtomicInteger();

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 信道
     */
    private String channel;

    /**
     * 缓存区
     */
    private String region;

    /**
     * 存数据的缓存key
     */
    private String data;

    /**
     * 存乐观锁的缓存key
     */
    private String version;

    /**
     * 锁缓存key
     */
    private String readLock;

    /**
     * 写锁
     */
    private String writeLock;

    private SyncKeys() {
    }

    /**
     * 根据缓存key生成对应的缓存同步所需的key值集合
     *
     * @param region 缓存域
     * @param key    缓存key
     * @return 缓存同步所需的key值集合
     */
    public static synchronized SyncKeys of(String region, Object key) {
        String keyString = key.toString();
        String id = region + keyString;

        SyncKeys v = KEY_INFO.computeIfAbsent(id, k -> {
            SyncKeys syncKeys = SyncKeys.builder().data(keyString).region(region).id(k).build();
            syncKeys.setChannel(k + "_channel");
            syncKeys.setVersion(k + "_version");
            syncKeys.setReadLock(k + "_readLock");
            syncKeys.setWriteLock(k + "_writeLock");


            return syncKeys;
        });

        CHANNEL_KEY_INFO.putIfAbsent(v.getChannel(), v);
        return v;
    }

    public static synchronized SyncKeys of(String region, Object key, Duration timeout) {
        String keyString = key.toString();
        String id = region + keyString;

        SyncKeys v = KEY_INFO.computeIfAbsent(id, k -> {
            SyncKeys syncKeys;
            if (timeout.equals(Duration.ZERO)) {
                syncKeys = SyncKeys.builder().data(keyString).region(region).id(k).build();
            } else {
                syncKeys = new SyncKeysWithTimeout(timeout);
                syncKeys.setData(keyString);
                syncKeys.setRegion(region);
            }

            syncKeys.setChannel(k + "_channel");
            syncKeys.setVersion(k + "_version");
            syncKeys.setReadLock(k + "_readLock");
            syncKeys.setWriteLock(k + "_writeLock");


            return syncKeys;
        });

        CHANNEL_KEY_INFO.putIfAbsent(v.getChannel(), v);
        return v;
    }

    /**
     * 根据信道生成对应的缓存同步所需的key值集合
     *
     * @param channel 信道
     * @return 缓存同步所需的key值集合
     */
    @SneakyThrows
    public static synchronized SyncKeys of(String channel) {
        SyncKeys syncKeys = CHANNEL_KEY_INFO.get(channel);
        if (syncKeys == null) {
            throw new CacheSyncException("未找到缓存同步相关key信息");
        }
        return syncKeys;
    }

    public static void remove(SyncKeys syncKeys) {
        KEY_INFO.remove(syncKeys.getRegion() + syncKeys.getData());
        CHANNEL_KEY_INFO.remove(syncKeys.getRegion() + syncKeys.getChannel());
    }

    public static class SyncKeysWithTimeout extends SyncKeys {
        private Duration timeout;

        public SyncKeysWithTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }
}
