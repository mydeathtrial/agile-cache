package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.redis.AgileRedisCacheManager;
import cloud.agileframework.cache.support.redis.CustomJackson2Module;
import cloud.agileframework.cache.support.redis.GenericRedisSerializer;
import cloud.agileframework.cache.support.redis.SecondCacheSerializerProvider;
import com.fasterxml.jackson.databind.Module;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;
import java.util.stream.Collectors;

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
    public SecondCacheSerializerProvider secondCacheSerializerProvider(ObjectProvider<Module> moduleObjectProvider) {
        List<Module> list = moduleObjectProvider.orderedStream().collect(Collectors.toList());
        list.add(new CustomJackson2Module());
        return new GenericRedisSerializer(list);
    }
}
