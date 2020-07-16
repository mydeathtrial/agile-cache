package com.agile.common.config;

import com.agile.common.properties.EhCacheProperties;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author 佟盟 on 2017/10/8
 */
@Configuration
@EnableConfigurationProperties(value = {EhCacheProperties.class})
@ConditionalOnClass({Cache.class, EhCacheCacheManager.class})
@ConditionalOnMissingBean({org.springframework.cache.CacheManager.class})
@ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "ehcache")
public class EhCacheAutoConfiguration {
    private final EhCacheProperties ehCacheProperties;

    public EhCacheAutoConfiguration(EhCacheProperties ehCacheProperties) {
        this.ehCacheProperties = ehCacheProperties;
    }

    @Bean
    public EhCacheCacheManager cacheManager(CacheManager ehCacheCacheManager) {
        return new EhCacheCacheManager(ehCacheCacheManager);
    }

    @Bean
    public CacheManager ehCacheCacheManager() {
        return new CacheManager(configuration());
    }

    public net.sf.ehcache.config.Configuration configuration() {
        DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration().path(ehCacheProperties.getPath());

        net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration().diskStore(diskStoreConfiguration);

        configuration.setName(ehCacheProperties.getDefaultConfigName());
        Map<String, CacheConfiguration> regions = ehCacheProperties.getRegions();

        for (Map.Entry<String, CacheConfiguration> entry : regions.entrySet()) {
            String name = entry.getKey();
            CacheConfiguration regionConfig = entry.getValue();

            regionConfig.setName(name);
            if (ehCacheProperties.getDefaultConfigName().equals(name)) {
                configuration.setDefaultCacheConfiguration(regionConfig);
                configuration.cache(regionConfig.clone().name("hibernate.org.hibernate.cache.spi.TimestampsRegion"));
                configuration.cache(regionConfig.clone().name("hibernate.org.hibernate.cache.spi.QueryResultsRegion"));
            } else {
                configuration.cache(regionConfig);
            }
        }
        return configuration;
    }
}
