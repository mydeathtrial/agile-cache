package com.agile;

import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.cache.sync.SyncKeys;
import cloud.agileframework.cache.util.CacheUtil;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 佟盟
 * 日期 2020/7/14 18:03
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class CacheTest implements Serializable {
    private final Logger logger = LoggerFactory.getLogger(CacheTest.class);
    @Autowired(required = false)
    private AgileRedisCacheManager redisCacheManager;

    @Test
    public void cover() {
        String key = "test1";
        int value = 1;
        CacheUtil.put(key, value);
        String v = CacheUtil.get(key, String.class);
        Assert.assertEquals("1", v);
    }

    @Test
    public void keys() {
        String key1 = "prefix1";
        String key2 = "prefix2";
        CacheUtil.put(key1, "1");
        CacheUtil.put(key2, "1");
        List<String> keys = CacheUtil.getCache().keys("prefix*");
        Assert.assertEquals(keys, Lists.newArrayList(key1, key2));
    }

    @Test
    public void evict() throws IOException {
        String key = "evict";
        CacheUtil.put(key, "1");
        CacheUtil.evict(key);
        Assert.assertNull(CacheUtil.get(key));
    }

    @Test
    public void map() {
        CacheUtil.put("map", new HashMap<String, String>());
        CacheUtil.addToMap("map", "k1", "1");
        CacheUtil.addToMap("map", "k2", "v2");
        CacheUtil.addToMap("map", "k3", "v4");
        CacheUtil.addToMap("map", "k3", "v3");
        CacheUtil.removeFromMap("map", "k2");
        Assert.assertEquals(1, (int) CacheUtil.getFromMap("map", "k1", int.class));
        Assert.assertNull(CacheUtil.getFromMap("map", "k2"));
        Assert.assertEquals(2, CacheUtil.get("map", Map.class).size());
    }

    @Test
    public void set() throws IOException {
        HashSet<Object> set = new HashSet<>();
        set.add("1");
        final String setKey = "set";
        CacheUtil.put(setKey, set);
        CacheUtil.addToSet(setKey, "1");
        CacheUtil.addToSet(setKey, 2);
        CacheUtil.addToSet(setKey, 2);
        CacheUtil.addToSet(setKey, 3);
        CacheUtil.removeFromSet(setKey, 3);
        Assert.assertTrue(CacheUtil.get(setKey, Set.class).contains(2));
        Assert.assertEquals(2, CacheUtil.get(setKey, Set.class).size());
    }

    @Test
    public void list() {
        ArrayList<Object> list = new ArrayList<>();
        final String listKey = "set";
        list.add("1");
        CacheUtil.put(listKey, list);
        CacheUtil.addToList(listKey, "2");
        CacheUtil.addToList(listKey, 3);
        CacheUtil.addToList(listKey, 3);
        CacheUtil.removeFromList(listKey, 0);
        Assert.assertEquals("2", CacheUtil.getFromList(listKey, 0));
        Assert.assertEquals(3, CacheUtil.get(listKey, List.class).size());
    }

    @Test
    public void lock() {
        String lockKey = "testLock";
        List<Thread> all = Lists.newArrayList();
        AtomicInteger lockCount = new AtomicInteger(0);
        int i = 9;
        while (i > 0) {
            all.add(new Thread(() -> {
                final boolean lockStatus = CacheUtil.lock(lockKey);

                logger.info("线程" + Thread.currentThread().getName() + "抢锁：" + (lockStatus ? "开着呢" : "锁着呢"));

                if (lockStatus) {
                    lockCount.addAndGet(1);
                }
            }));
            i--;
        }
        all.forEach(Thread::start);

        all.forEach(a -> {
            try {
                a.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Assert.assertEquals(lockCount.get(), 1);
    }

    @Test
    public void testFutureTask() throws ExecutionException, InterruptedException {
        Callable<Integer> callable = () -> {
            System.out.println(Thread.currentThread().getName());
            int a = 10;
            a += 10;
            return a;
        };

        FutureTask<Integer> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();

        System.out.println(futureTask.get());
    }

    @Before
    public void before(){
        CacheUtil.clear();
    }

    AtomicInteger lockCount = new AtomicInteger(0);
    /**
     * 测试多线程
     */
    @Test
    public void testMultipartyThread() throws IOException, InterruptedException {
        String lockKey = "reKey";
        List<Thread> all = Lists.newArrayList();

        int i = 9;
        while (i > 0) {
            all.add(new Thread(() -> {
                final int value = lockCount.addAndGet(1);

                CacheUtil.put(lockKey, value);
            }));
            i--;
        }
        all.forEach(Thread::start);

        all.forEach(a -> {
            try {
                a.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        if(redisCacheManager!=null){
            String region = CacheUtil.getCache().getName();
            SyncKeys syncKeys = SyncKeys.of(region, lockKey);
            Integer redisVersion = redisCacheManager.getCache(region).get(syncKeys.getVersion(), int.class);
            int ehcacheVersion = syncKeys.getVersionData().get();
        }
    }

    /**
     * 测试过期
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTimeout() throws InterruptedException {
        final String testKey = "testSecondCache";
        CacheUtil.put(testKey, "111", Duration.ofSeconds(2));
        Thread.sleep(2000);
        Assert.assertNull(CacheUtil.get(testKey, String.class));
    }

    /**
     * 测试引用
     *
     */
    @Test
    public void testQuote() {
        final String testKey = "testQuote";

        Demo s = new Demo();
        s.setA("1");
        CacheUtil.put(testKey, s);
        s.setA("2");

        CacheUtil.containKey(testKey);
        Assert.assertEquals(CacheUtil.get(testKey, Demo.class).getA(), "1");
    }

}
