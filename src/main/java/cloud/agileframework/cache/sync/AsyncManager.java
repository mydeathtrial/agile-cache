package cloud.agileframework.cache.sync;

import java.util.concurrent.*;

/**
 * 异步任务管理器
 *
 * @author mydeathtrial
 */
public class AsyncManager {
    /**
     * 异步操作任务调度线程池
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,Integer.MAX_VALUE,3,TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    /**
     * 单例模式
     */
    private AsyncManager() {
    }

    private static AsyncManager single;

    private static AsyncManager getSingle() {
        if (single != null) {
            return single;
        }

        synchronized (AsyncManager.class) {
            if (single != null) {
                return single;
            }
            single = new AsyncManager();
            return single;
        }
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public static void execute(Runnable task) {
        getSingle().executor.execute(task);
    }
}
