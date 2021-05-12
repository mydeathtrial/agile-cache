package cloud.agileframework.cache.support.redis;

import cloud.agileframework.common.util.bytes.ByteUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.security.Key;

/**
 * @author 佟盟
 * 日期 2021-05-10 13:46
 * 描述 Key类型相关的正反序列化配置
 * @version 1.0
 * @since 1.0
 */
public class KeySerializerAbout {
    public static final KeySerializer KEY_SERIALIZER = new KeySerializer();
    public static final KeyDeserialization KEY_DESERIALIZATION = new KeyDeserialization();

    private static class KeySerializer extends JsonSerializer<Key> {

        @Override
        public void serialize(Key value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("@class", Key.class.getCanonicalName());
            gen.writeObjectField("@value", ByteUtil.toBase64(SerializationUtils.serialize(value)));
            gen.writeEndObject();
        }
    }

    private static class KeyDeserialization extends JsonDeserializer<Key> {

        @Override
        public Key deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            p.nextToken();
            String data = p.getText();
            return SerializationUtils.deserialize(ByteUtil.toByte(data));
        }
    }
}
