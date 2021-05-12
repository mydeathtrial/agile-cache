package cloud.agileframework.cache.support.redis;

import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author 佟盟
 * 日期 2021-05-10 15:43
 * 描述 二级缓存存储数据序列化定制接口
 * @version 1.0
 * @since 1.0
 */
public interface SecondCacheSerializerProvider {
    /**
     * 序列化
     *
     * @param object 对象
     * @return 字节数组
     * @throws SerializationException 异常
     */
    byte[] serialize(Object object) throws SerializationException;

    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @return 对象
     * @throws SerializationException 异常
     */
    Object deserialize(byte[] bytes) throws SerializationException;
}
