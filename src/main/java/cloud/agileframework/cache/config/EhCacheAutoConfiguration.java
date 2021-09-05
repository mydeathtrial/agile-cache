package cloud.agileframework.cache.config;

import cloud.agileframework.cache.properties.EhCacheProperties;
import cloud.agileframework.cache.support.ehcache.AgileEhCacheCacheManager;
import cloud.agileframework.cache.support.ehcache.SyncCacheEventListener;
import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.cache.sync.RedisSyncCache;
import cloud.agileframework.cache.sync.SyncCache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * @author 佟盟 on 2017/10/8
 */
@Configuration
@EnableConfigurationProperties(value = {EhCacheProperties.class})
@ConditionalOnClass({CacheManager.class})
@ConditionalOnProperty(name = "enabled", prefix = "spring.ehcache", matchIfMissing = true)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class EhCacheAutoConfiguration implements CacheAutoConfiguration {
    @Autowired
    private EhCacheProperties ehCacheProperties;

    @Bean
    @Primary
    public AgileEhCacheCacheManager agileEhCacheCacheManager(CacheManager ehCacheCacheManager) {
        return new AgileEhCacheCacheManager(ehCacheCacheManager);
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

    /**
     * 如果redis激活，则增加二级缓存
     *
     * @return 二级缓存工具
     */
    @Bean
    @ConditionalOnBean(AgileRedisCacheManager.class)
    @ConditionalOnProperty(name = "sync", prefix = "spring.ehcache")
    public RedisSyncCache syncCache() {
        return new RedisSyncCache();
    }

    @Bean
    @ConditionalOnMissingBean(SyncCache.class)
    public SyncCache syncCacheDefault() {
        return new SyncCache() {
        };
    }

    /**
     * 事件监听器
     *
     * @return 事件监听
     */
    @Bean
    public SyncCacheEventListener syncCacheEventListener() {
        return new SyncCacheEventListener();
    }
}
