package cloud.agileframework.cache.support.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

/**
 * @author 佟盟
 * 日期 2021-03-30 16:18
 * 描述 redis序列化工具
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class GenericRedisSerializer implements RedisSerializer<Object>, SecondCacheSerializerProvider {
    public static final String COULD_NOT_SERIALIZE = "Could not serialize: ";
    public static final String COULD_NOT_DESERIALIZE = "Could not deserialize: ";
    private final ParserConfig DEFAULT_REDIS_CONFIG = new ParserConfig();
    private final GenericJackson2JsonRedisSerializer JACKSON2_JSON_REDIS_SERIALIZER;
    private final JdkSerializationRedisSerializer JDK_SERIALIZATION_REDIS_SERIALIZER = new JdkSerializationRedisSerializer();

    public GenericRedisSerializer(ObjectProvider<Module> moduleObjectProvider) {
        DEFAULT_REDIS_CONFIG.setAutoTypeSupport(true);
        DEFAULT_REDIS_CONFIG.setJacksonCompatible(true);
        DEFAULT_REDIS_CONFIG.putDeserializer(java.util.Date.class, MyDateCodec.instance);

        ObjectMapper objectMapper = new ObjectMapper();
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(objectMapper, null);

        /* 注册其他配置 */
        moduleObjectProvider.forEach(objectMapper::registerModule);

        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        /* 屏蔽反序列化过程中的一些异常 */
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);

        /* 忽略get方法，所有属性都序列化 */
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        JACKSON2_JSON_REDIS_SERIALIZER = new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        byte[] result = new byte[0];
        Object fResult = null;
        if (object == null) {
            return result;
        }
        try {
            result = JACKSON2_JSON_REDIS_SERIALIZER.serialize(object);
            fResult = JACKSON2_JSON_REDIS_SERIALIZER.deserialize(result);
        } catch (Exception e) {
            log.debug(COULD_NOT_SERIALIZE, e);
        }

        try {
            if (!object.equals(fResult)) {
                result = JSON.toJSONBytes(object, SerializerFeature.WriteClassName, SerializerFeature.IgnoreNonFieldGetter, SerializerFeature.WriteMapNullValue);
                fResult = JSON.parseObject(new String(result, IOUtils.UTF8), Object.class, DEFAULT_REDIS_CONFIG);
            }
        } catch (Exception e) {
            log.debug(COULD_NOT_SERIALIZE, e);
            result = null;
        }

        try {
            if (!object.equals(fResult)) {
                result = JDK_SERIALIZATION_REDIS_SERIALIZER.serialize(object);
            }
        } catch (Exception e) {
            log.debug(COULD_NOT_SERIALIZE, e);
            result = null;
        }

        return result;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {

        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Object result = null;
        byte[] fResult = new byte[0];
        try {
            result = JSON.parseObject(new String(bytes, IOUtils.UTF8), Object.class, DEFAULT_REDIS_CONFIG);
            fResult = serialize(result);
        } catch (Exception e) {
            log.debug(COULD_NOT_DESERIALIZE, e);
        }

        try {
            if (!Arrays.equals(bytes, fResult)) {
                result = JACKSON2_JSON_REDIS_SERIALIZER.deserialize(bytes);
            }
        } catch (Exception e) {
            log.debug(COULD_NOT_DESERIALIZE, e);
            result = null;
        }

        try {
            if (result == null) {
                result = JDK_SERIALIZATION_REDIS_SERIALIZER.deserialize(bytes);
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}
