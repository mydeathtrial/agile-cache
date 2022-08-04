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

    /**
     * 二级缓存同步执行
     *
     * @param syncKeys 相关的缓存key信息
     * @param supplier 执行方法
     * @param opType   执行类型
     * @param <T>      返回的数据类型
     * @return 执行方法返回值
     */
    default <T> T sync(SyncKeys syncKeys, Supplier<T> supplier, OpType opType) {
        return supplier.get();
    }

    default void sync(SyncKeys syncKeys, OpType opType) {
    }

    default void clear(String region) {
    }
}
