package cloud.agileframework.cache.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 佟盟
 * 日期 2021-03-15 14:38
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@ConfigurationProperties(prefix = "agile.cache")
public class AgileCacheProperties {
    private boolean enabled = false;
}
