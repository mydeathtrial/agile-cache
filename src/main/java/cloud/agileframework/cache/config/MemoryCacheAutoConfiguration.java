package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AgileCacheManagerInterface;
import cloud.agileframework.cache.support.memory.MemoryCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 佟盟
 * 日期 2020/7/17 11:59
 * 描述 内存缓存配置
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@AutoConfigureAfter({EhCacheAutoConfiguration.class, RedisAutoConfiguration.class})
public class MemoryCacheAutoConfiguration extends CacheAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CacheProperties cacheProperties;

    @Override
    @Bean
    @ConditionalOnMissingBean(AgileCacheManagerInterface.class)
    public MemoryCacheManager agileCacheManager() {
        if (CacheType.REDIS == cacheProperties.getType() ||
                CacheType.EHCACHE == cacheProperties.getType()) {
            log.error("检测到您所配置的缓存介质，生成条件无法满足，已启用默认的内存缓存介质");
        }
        return new MemoryCacheManager();
    }
}
