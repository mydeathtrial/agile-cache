package cloud.agileframework.cache.support.redis;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Type;

/**
 * @author 佟盟
 * 日期 2021-03-30 16:18
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class MyGenericFastJsonRedisSerializer implements RedisSerializer<Object> {
    private static final ParserConfig DEFAULT_REDIS_CONFIG = new ParserConfig();
    static {
        DEFAULT_REDIS_CONFIG.setAutoTypeSupport(true);
        DEFAULT_REDIS_CONFIG.putDeserializer(java.util.Date.class, MyDateCodec.instance);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, IOUtils.UTF8), Object.class, DEFAULT_REDIS_CONFIG);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
