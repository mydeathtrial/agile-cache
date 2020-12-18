package cloud.agileframework.cache.config;

import cloud.agileframework.cache.properties.EhCacheProperties;
import cloud.agileframework.cache.support.ehcache.AgileEhCacheCacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author 佟盟 on 2017/10/8
 */
@Configuration
@EnableConfigurationProperties(value = {EhCacheProperties.class})
@ConditionalOnClass({CacheManager.class})
@ConditionalOnMissingBean({org.springframework.cache.CacheManager.class})
@ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "ehcache")
public class EhCacheAutoConfiguration extends CacheAutoConfiguration{
    private final EhCacheProperties ehCacheProperties;

    public EhCacheAutoConfiguration(EhCacheProperties ehCacheProperties) {
        this.ehCacheProperties = ehCacheProperties;
    }

    @Override
    @Bean
    AgileEhCacheCacheManager agileCacheManager() {
        return new AgileEhCacheCacheManager(ehCacheCacheManager());
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
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
