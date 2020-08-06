package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AgileCacheManager;
import cloud.agileframework.cache.support.ehcache.AgileEhCacheCacheManager;
import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.cache.util.CacheUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author 佟盟
 * 日期 2020/8/6 10:17
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
public class AgileCacheAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "ehcache")
    AgileEhCacheCacheManager agileEhCacheCacheManager(EhCacheCacheManager cacheManager) {
        return new AgileEhCacheCacheManager(cacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "redis")
    AgileRedisCacheManager agileRedisCacheManager(RedisCacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
        return new AgileRedisCacheManager(cacheManager, redisConnectionFactory);
    }

    @Override
    public void afterPropertiesSet() {
        CacheUtil.setAgileCacheManager(applicationContext.getBean(AgileCacheManager.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
