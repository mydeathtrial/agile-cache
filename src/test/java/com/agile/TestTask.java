package com.agile;

import cloud.agileframework.cache.support.AgileCacheManagerInterface;
import cloud.agileframework.cache.util.CacheUtil;
import cloud.agileframework.spring.util.BeanUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

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
public class TestTask {
    private final Logger logger = LoggerFactory.getLogger(TestTask.class);

    @Autowired
    private AgileCacheManagerInterface agileCacheManagerInterface;

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
    public void map() {
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
    public void set() {
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
    public void list() {
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
        CacheUtil.unlock("tudou");
    }

    @Test
    public void lockTimeout() throws InterruptedException {
        CacheUtil.unlock("tudou");
        boolean lock = CacheUtil.lock("tudou", Duration.ofSeconds(10));
        logger.info("上锁：" + (lock ? "成功" : "失败"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        Thread.sleep(10 * 1000);
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        CacheUtil.unlock("tudou");
    }

    @Test
    public void unlockTimeout() throws InterruptedException {
        CacheUtil.unlock("tudou");
        boolean lock = CacheUtil.lock("tudou");
        logger.info("上锁：" + (lock ? "成功" : "失败"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        CacheUtil.unlock("tudou", Duration.ofSeconds(10));
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        Thread.sleep(10 * 1000);
        logger.info("锁状态：" + (CacheUtil.lock("tudou") ? "开着呢" : "锁着呢"));
        CacheUtil.unlock("tudou");
    }
}
