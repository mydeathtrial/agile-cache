package cloud.agileframework.cache.sync;

import cloud.agileframework.cache.util.BeanUtil;
import org.springframework.transaction.annotation.Transactional;

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

    private static volatile AsyncManager single;

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
     * 处理事务用的
     */
    private static final RunnerWrapper RUNNER_WRAPPER = BeanUtil.getApplicationContext().getBean(RunnerWrapper.class);

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public static void execute(Runner task) {
        getSingle().executor.execute(() -> RUNNER_WRAPPER.run(task));
    }

    public interface Runner {
        /**
         * 执行器
         */
        void run();
    }

    public static class RunnerWrapper {
        @Transactional(rollbackFor = Exception.class)
        public void run(Runner runner) {
            runner.run();
        }
    }
}
