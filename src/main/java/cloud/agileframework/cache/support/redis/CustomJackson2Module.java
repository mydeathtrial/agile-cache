package cloud.agileframework.cache.support.redis;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author 佟盟
 * 日期 2021-05-11 14:33
 * 描述 自定义jackson2的序列化类型
 * @version 1.0
 * @since 1.0
 */
public class CustomJackson2Module extends SimpleModule {
    public CustomJackson2Module() {
        super(CustomJackson2Module.class.getName(), new Version(1, 0, 0, (String)null, (String)null, (String)null));
        addDeserializer(Date.class, new DateDeserializers.DateDeserializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        addDeserializer(Timestamp.class, new DateDeserializers.TimestampDeserializer());
        addSerializer(Key.class,KeySerializerAbout.KEY_SERIALIZER);
        addDeserializer(Key.class,KeySerializerAbout.KEY_DESERIALIZATION);
    }
}
