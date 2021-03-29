package com.agile;

import cloud.agileframework.cache.sync.OptimisticLockCheckError;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.spring.util.BeanUtil;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 佟盟
 * 日期 2020/7/14 18:03
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class TestTask implements Serializable {
    private final Logger logger = LoggerFactory.getLogger(TestTask.class);

    @Before
    public void t() {
        Object a = CacheUtil.getCache("sd");
    }

    @Test
    public void add() {
        StringRedisTemplate redisTemplate = BeanUtil.getBean(StringRedisTemplate.class);
        redisTemplate.convertAndSend("tudou", "1");
        CacheUtil.put("tudou", "1");
        logger.info(CacheUtil.get("tudou", String.class));
    }

    @Test
    public void get() {
        CacheUtil.put("tudou1", "1");
        CacheUtil.put("tudou2", "1");
        CacheUtil.getCache().keys("tudou*");
        logger.info(CacheUtil.get("tudou", String.class));
    }

    @Test
    public void delete() {
        CacheUtil.put("tudou", "1");
        CacheUtil.evict("tudou");
        logger.info(CacheUtil.get("tudou", String.class));
    }

    @Test
    public void map() throws IOException {
        CacheUtil.put("map", new HashMap<String, String>());
        CacheUtil.addToMap("map", "1", "v1");
        CacheUtil.addToMap("map", "2", "v2");
        CacheUtil.addToMap("map", "3", "v4");
        CacheUtil.addToMap("map", "3", "v3");
        CacheUtil.removeFromMap("map", "2");
        logger.info(CacheUtil.getFromMap("map", "1", String.class));
        logger.info(CacheUtil.get("map", Map.class).toString());
    }

    @Test
    public void set() throws IOException {
        HashSet<Object> set = new HashSet<>();
        set.add("1");
        CacheUtil.put("set", set);
        CacheUtil.addToSet("set", "1");
        CacheUtil.addToSet("set", 2);
        CacheUtil.addToSet("set", 4);
        CacheUtil.addToSet("set", 3);
        CacheUtil.removeFromSet("set", 2);
        logger.info(CacheUtil.get("set", Set.class).toString());
    }

    @Test
    public void list() throws IOException {
        ArrayList<Object> list = new ArrayList<>();
        list.add("1");
        CacheUtil.put("list", list);
        CacheUtil.addToList("list", "1");
        CacheUtil.addToList("list", 2);
        CacheUtil.addToList("list", 4);
        CacheUtil.addToList("list", 3);
        CacheUtil.removeFromList("list", 2);
        logger.info(CacheUtil.getFromList("list", 1, String.class));
        logger.info(CacheUtil.get("list", List.class).toString());
    }

    @Test
    public void lock() {
        CacheUtil.unlock("tudou");
        boolean lock = CacheUtil.lock("tudou");
        logger.info("上锁：" + (lock ? "成功" : "失败"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        logger.info("解锁");
        CacheUtil.unlock("tudou");
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
    }

    @Test
    public void lockTimeout() throws InterruptedException {
        boolean lock = CacheUtil.lock("tudou");
        logger.info("上锁：" + (lock ? "成功" : "失败"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        Thread.sleep(10 * 1000);
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        CacheUtil.unlock("tudou");
    }

    @Test
    public void unlockTimeout() throws IOException {
        final String key = "tudou";
        Thread t1 = new Thread(() -> {
            try {
                CacheUtil.lock(key);
                for (int i = 0; i < 5; i++) {
                    CacheUtil.put(key, i);
                    System.out.println("thread1 put " + i + " to " + key);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CacheUtil.unlock(key);
                System.out.println("thread1 end.");
            }
        });

        Thread t2 = new Thread(new Runnable() {

            boolean canRead = false;
            boolean canWrite = false;

            @Override
            public void run() {
                try {
                    // 测试一
                    while (true) {
                        Thread.sleep(300);
                        if (CacheUtil.lock(key)) {
                            System.out.println("thread2 get write lock success.");
                            canWrite = true;
                        } else {
                            System.out.println("thread2 cannot get write lock.");
                        }

                        if (canWrite) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("thread2 end.");
                }
            }
        });

        t1.start();
        t2.start();
//        System.in.read();
    }

    /**
     * 测试多线程
     *
     * @throws IOException
     */
    @Test
    public void testMultipartyThread() throws IOException {
        final String testKey = "tudou";

        List<Thread> all = Lists.newArrayList();
        int i = 100;
        while (i > 0) {
            int current = i--;
            all.add(new Thread() {
                @Override
                public void run() {
                    boolean success = false;

                    while (!success){
                        try {
                            CacheUtil.put(testKey, current);
                            success = true;
                        }catch (OptimisticLockCheckError e){
                            System.out.println("乐观锁失败");
                        }
                    }
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    final Long aLong = CacheUtil.get(testKey, Long.class);
//                    if (aLong != current) {
//                        throw new RuntimeException("出事儿了");
//                    }else{
//                        System.out.println("正常" + aLong);
//                    }
                }
            });
        }

        all.forEach(a -> {
            a.start();
        });

        all.forEach(a->{
            try {
                a.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("主线程" + CacheUtil.get(testKey, String.class));
//        System.in.read();
    }

    /**
     * 测试乐观锁
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCAS() throws IOException, InterruptedException {
        final String testKey = "tudou";
        CacheUtil.put(testKey, "old");

        Thread.sleep(20000);

        CacheUtil.put(testKey, "new");

        System.out.println("主线程" + CacheUtil.get(testKey, String.class));
//        System.in.read();
    }

    /**
     * 测试过期
     *
     * @throws IOException
     */
    @Test
    public void testTimeOut() throws IOException {
        final String testKey = "tudou";

        CacheUtil.put(testKey, "timeout", Duration.ofSeconds(2));
        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("主线程" + CacheUtil.get(testKey, String.class));
//        System.in.read();
    }

    /**
     * 测试过期
     *
     * @throws IOException
     */
    @Test
    public void testUpdate() throws IOException, InterruptedException {
        final String testKey = "tudou";

        Demo s = new Demo();
        s.setA("1");
        CacheUtil.put(testKey, s);

        Thread.sleep(6000);
        s.setA("2");

        System.out.println("主线程" + CacheUtil.get(testKey, Demo.class));
//        System.in.read();
    }

}
