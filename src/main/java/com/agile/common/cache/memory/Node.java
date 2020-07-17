package com.agile.common.cache.memory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 佟盟
 * 日期 2020/7/17 10:44
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class Node {
    private long timeout;
    private Object value;

    public Node(long timeout, Object value) {
        this.timeout = timeout;
        this.value = value;
    }

    public long getTimeout() {
        return timeout;
    }

    public Object getValue() {
        return value;
    }
}
