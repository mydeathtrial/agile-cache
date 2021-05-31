package cloud.agileframework.cache.support.ehcache;

import lombok.Data;

import java.util.Objects;

/**
 * @author 佟盟
 * 日期 2021-05-28 15:45
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
public class TransmitKey {
    /**
     * 真实的key
     */
    private final Object key;
    /**
     * 是否传递，true则不同多级缓存
     */
    private boolean transmit = true;

    public static TransmitKey of(Object key) {
        return new TransmitKey(key);
    }

    public static TransmitKey of(Object key, boolean transmit) {
        TransmitKey result = of(key);
        result.setTransmit(transmit);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransmitKey)) {
            return Objects.equals(key, o);
        }
        TransmitKey that = (TransmitKey) o;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        if(getKey() instanceof String){
            return getKey().hashCode();
        }
        return Objects.hash(getKey());
    }
}
