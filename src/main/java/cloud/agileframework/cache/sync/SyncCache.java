package cloud.agileframework.cache.sync;

import java.util.function.Supplier;

/**
 * @author 佟盟
 * 日期 2021-02-19 10:33
 * 描述 缓存同步接口
 * @version 1.0
 * @since 1.0
 */
public interface SyncCache {

    default <T> T sync(String region, String key, Supplier<T> supplier, OpType opType) {
        return supplier.get();
    }

    default void clear(String region) {
    }
}
