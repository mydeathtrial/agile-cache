package cloud.agileframework.cache.config;

import cloud.agileframework.cache.support.AgileCacheManager;
import cloud.agileframework.cache.util.CacheUtil;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author 佟盟
 * 日期 2020-12-18 10:36
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public abstract class CacheAutoConfiguration implements InitializingBean {
    /**
     * 提供Agile的缓存管理器
     * @return Agile的缓存管理器
     */
    abstract AgileCacheManager agileCacheManager();


    @Override
    public void afterPropertiesSet() throws Exception {
        CacheUtil.setAgileCacheManagerInterface(agileCacheManager());
    }
}
