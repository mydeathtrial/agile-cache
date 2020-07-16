package com.agile;

import com.agile.common.util.CacheUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void add() throws Exception {
        CacheUtil.put("tudou","1");
        logger.info(CacheUtil.get("tudou",String.class));
    }

    @Test
    public void delete() throws Exception {
        CacheUtil.put("tudou","1");
        CacheUtil.evict("tudou");
        logger.info(CacheUtil.get("tudou",String.class));
    }
}
