package cloud.agileframework.cache.support;

import cloud.agileframework.common.util.clazz.TypeReference;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2019/7/15 13:50
 * 描述 缓存实例功能集接口
 * @version 1.0
 * @since 1.0
 */
public interface AgileCache extends Cache {

    /**
     * 过期存储
     *
     * @param key     索引
     * @param value   值
     * @param timeout 过期时间
     */
    void put(Object key, Object value, Duration timeout);

    /**
     * 获取
     *
     * @param key           索引
     * @param typeReference 类型
     * @param <T>           泛型
     * @return 值
     */
    <T> T get(Object key, TypeReference<T> typeReference);

    /**
     * 判断
     *
     * @param key 索引
     * @return 是否
     */
    boolean containKey(Object key);

    /**
     * 向Map中添加数据
     *
     * @param mapKey map索引
     * @param key    key
     * @param value  value
     */
    void addToMap(Object mapKey, Object key, Object value);

    /**
     * 查询Map中索引key对应的value数据
     *
     * @param mapKey map索引
     * @param key    key
     * @return 值
     */
    Object getFromMap(Object mapKey, Object key);

    /**
     * 查询Map中索引key对应的value数据
     *
     * @param mapKey map索引
     * @param key    key
     * @param clazz  结果转换类型
     * @return 值
     */
    <T> T getFromMap(Object mapKey, Object key, Class<T> clazz);

    /**
     * 从Map中删除索引key
     *
     * @param mapKey map索引
     * @param key    key
     */
    void removeFromMap(Object mapKey, Object key);

    /**
     * 向List中添加节点
     *
     * @param listKey list索引
     * @param node    节点数据
     */
    void addToList(Object listKey, Object node);

    /**
     * 从List中获取下标为inde下的节点数据
     *
     * @param listKey list索引
     * @param index   节点下标
     * @return 值
     */
    Object getFromList(Object listKey, int index);

    /**
     * 从List中获取下标为inde下的节点数据
     *
     * @param listKey list索引
     * @param index   节点下标
     * @param clazz   结果转换类型
     * @return 值
     */
    <T> T getFromList(Object listKey, int index, Class<T> clazz);

    /**
     * 从List中删除下标节点
     *
     * @param listKey list索引
     * @param index   节点下标
     */
    void removeFromList(Object listKey, int index);

    /**
     * 向set中添加节点
     *
     * @param setKey set索引
     * @param node   节点数据
     */
    void addToSet(Object setKey, Object node);

    /**
     * set中删除节点
     *
     * @param setKey set索引
     * @param node   节点数据
     */
    void removeFromSet(Object setKey, Object node);

    /**
     * 分布式同步锁
     *
     * @param lock 锁标识
     * @return 是否加锁成功
     */
    boolean lock(Object lock);


    /**
     * 解锁
     *
     * @param lock 锁标识
     */
    void unlock(Object lock);

    /**
     * 模糊匹配key
     *
     * @param key key匹配表达式
     * @return 集合
     */
    List<String> keys(Object key);
}
