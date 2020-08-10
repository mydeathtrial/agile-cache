package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AgileCacheManager;
import cloud.agileframework.cache.support.AgileCacheManagerInterface;
import cloud.agileframework.cache.util.CacheUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @author 佟盟
 * 日期 2020/8/6 10:17
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConditionalOnBean(AgileCacheManagerInterface.class)
@AutoConfigureAfter({CacheAutoConfiguration.class,
        EhCacheAutoConfiguration.class,
        RedisAutoConfiguration.class,
        MemoryCacheAutoConfiguration.class})
public class AgileCacheAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        CacheUtil.setAgileCacheManager(applicationContext.getBean(AgileCacheManager.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
