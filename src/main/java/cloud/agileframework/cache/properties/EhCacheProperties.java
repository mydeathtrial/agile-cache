package cloud.agileframework.cache.properties;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by 佟盟 on 2018/2/1
 */
@ConfigurationProperties(prefix = "spring.ehcache")
public class EhCacheProperties implements InitializingBean {
    /**
     * 默认配置
     */
    private String defaultConfigName = "common-cache";
    /**
     * 存储地址
     */
    private String path = "/ehcache";

    /**
     * 缓存区域配置
     */
    private Map<String, CacheConfiguration> regions = new HashMap<>();

    public String getDefaultConfigName() {
        return defaultConfigName;
    }

    public void setDefaultConfigName(String defaultConfigName) {
        this.defaultConfigName = defaultConfigName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, CacheConfiguration> getRegions() {
        return regions;
    }

    public void setRegions(Map<String, CacheConfiguration> regions) {
        this.regions = regions;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        regions.computeIfAbsent(defaultConfigName, a -> {
            final CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
            defaultCacheConfiguration.setDiskExpiryThreadIntervalSeconds(120);
            defaultCacheConfiguration.setDiskSpoolBufferSizeMB(30);
            defaultCacheConfiguration.setEternal(false);
            defaultCacheConfiguration.setMaxEntriesLocalDisk(10000000);
            defaultCacheConfiguration.setMaxEntriesLocalHeap(10000);
            defaultCacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
            defaultCacheConfiguration.setTimeToIdleSeconds(0);
            defaultCacheConfiguration.setTimeToLiveSeconds(0);
            return defaultCacheConfiguration;
        });
    }
}
