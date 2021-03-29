package cloud.agileframework.cache.sync;

import lombok.Builder;
import lombok.Data;

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
public class SyncKeys {

    /**
     * 版本号
     */
    private final AtomicInteger versionData = new AtomicInteger();

    private String channel;

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

    private String writeLock;
}
