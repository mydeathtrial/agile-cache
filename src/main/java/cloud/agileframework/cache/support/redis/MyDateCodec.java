package cloud.agileframework.cache.support.redis;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.DateCodec;

import java.lang.reflect.Type;

/**
 * @author 佟盟
 * 日期 2021-03-30 18:56
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class MyDateCodec extends DateCodec {
    public static final MyDateCodec instance = new MyDateCodec();

    @Override
    public <T> T cast(DefaultJSONParser parser, Type clazz, Object fieldName, Object val) {
        if (val == null) {
            return null;
        }

        if (val instanceof Number && clazz == java.sql.Timestamp.class) {
            return (T) new java.sql.Timestamp(((Number) val).longValue());
        }
        return super.cast(parser, clazz, fieldName, val);
    }
}
