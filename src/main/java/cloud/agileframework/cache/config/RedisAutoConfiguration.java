package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.cache.support.redis.GenericFstRedisSerializer;
import cloud.agileframework.cache.support.redis.Jackson2ModuleProvider;
import cloud.agileframework.cache.support.redis.SecondCacheSerializerProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author 佟盟
 * 日期 2020/8/00010 15:03
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConditionalOnClass({RedisCacheManager.class})
public class RedisAutoConfiguration implements CacheAutoConfiguration {

    @Bean
    public AgileRedisCacheManager agileRedisCacheManager(RedisCacheManager redisCacheManager, RedisConnectionFactory redisConnectionFactory) {
        return new AgileRedisCacheManager(redisCacheManager, redisConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheManager.class)
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.create(redisConnectionFactory);
    }

    /**
     * 注入序列化工具
     *
     * @return 定制化序列化工具
     */
    @Bean
    @ConditionalOnMissingBean(SecondCacheSerializerProvider.class)
    public SecondCacheSerializerProvider secondCacheSerializerProvider(ObjectProvider<Jackson2ModuleProvider> jackson2ModuleProviders) {
        return new GenericFstRedisSerializer();
    }
}
