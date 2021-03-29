package cloud.agileframework.cache.sync;

/**
 * @author 佟盟
 * 日期 2021-02-19 16:49
 * 描述 缓存同步异常
 * @version 1.0
 * @since 1.0
 */
public class CacheSyncException extends Exception {

    public CacheSyncException(String message) {
        super(message);
    }

    public CacheSyncException(Throwable cause) {
        super(cause);
    }
}
