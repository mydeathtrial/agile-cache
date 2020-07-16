package com.agile.common.properties;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by 佟盟 on 2018/2/1
 */
@ConfigurationProperties(prefix = "spring.ehcache")
public class EhCacheProperties {
    /**
     * 默认配置
     */
    private String defaultConfigName;
    /**
     * 存储地址
     */
    private String path;

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
}
