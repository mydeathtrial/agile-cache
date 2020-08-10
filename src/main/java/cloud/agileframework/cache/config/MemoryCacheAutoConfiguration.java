package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AgileCacheManagerInterface;
import cloud.agileframework.cache.support.memory.MemoryCacheManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@AutoConfigureAfter({EhCacheAutoConfiguration.class, RedisAutoConfiguration.class})
public class MemoryCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AgileCacheManagerInterface.class)
    public MemoryCacheManager memoryCacheManager() {
        return new MemoryCacheManager();
    }
}
