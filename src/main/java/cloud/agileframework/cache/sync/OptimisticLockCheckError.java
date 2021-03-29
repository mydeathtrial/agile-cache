package cloud.agileframework.cache.sync;

/**
 * @author 佟盟
 * 日期 2021-03-22 17:13
 * 描述 乐观锁检查失败
 * @version 1.0
 * @since 1.0
 */
public class OptimisticLockCheckError extends RuntimeException {
    public OptimisticLockCheckError() {
    }

    public OptimisticLockCheckError(String message) {
        super(message);
    }

    public OptimisticLockCheckError(String message, Throwable cause) {
        super(message, cause);
    }

    public OptimisticLockCheckError(Throwable cause) {
        super(cause);
    }

    public OptimisticLockCheckError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
