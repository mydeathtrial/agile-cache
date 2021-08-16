package cloud.agileframework.cache.support.redis;

import cloud.agileframework.common.util.serializer.FstUtil;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author 佟盟
 * 日期 2021-03-30 16:18
 * 描述 redis序列化工具
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class GenericFstRedisSerializer implements RedisSerializer<Object>, SecondCacheSerializerProvider {

	private static final GenericJackson2JsonRedisSerializer JACKSON_2_JSON_REDIS_SERIALIZER = new GenericJackson2JsonRedisSerializer();
	private static final GenericFastJsonRedisSerializer FAST_JSON_REDIS_SERIALIZER = new GenericFastJsonRedisSerializer();
	public static final String COULD_NOT_SERIALIZE = "Could not serialize: ";
	public static final String COULD_NOT_DESERIALIZE = "Could not deserialize: ";

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		byte[] result = new byte[0];
		if (object == null) {
			return result;
		}

		try {
			result = FstUtil.serialize(object);
		} catch (Exception e) {
			log.debug(COULD_NOT_SERIALIZE, e);
		}

		return result;
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {

		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Object result = null;
		try {
			result = FstUtil.deserialize(bytes);
		} catch (Exception e) {
			try {
				result = JACKSON_2_JSON_REDIS_SERIALIZER.deserialize(bytes);
			} catch (Exception e2) {
				e.addSuppressed(e2);
				try {
					result = FAST_JSON_REDIS_SERIALIZER.deserialize(bytes);
				} catch (Exception e3) {
					e.addSuppressed(e3);
					log.debug(COULD_NOT_DESERIALIZE, e);
				}
			}
		}

		return result;
	}
}
