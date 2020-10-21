package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "type", prefix = "spring.cache", havingValue = "redis")
@ConditionalOnClass({RedisCacheManager.class})
@ConditionalOnBean({RedisCacheManager.class})
public class RedisAutoConfiguration {
    @Bean
    AgileRedisCacheManager agileRedisCacheManager(RedisCacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
        return new AgileRedisCacheManager(cacheManager, redisConnectionFactory);
    }
}
