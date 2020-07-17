package com.agile.common.config;

import com.agile.common.cache.memory.MemoryCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "type", prefix = "spring.cache", matchIfMissing = true)
public class MemoryCacheAutoConfiguration {
    @Bean
    public MemoryCacheManager memoryCacheManager() {
        return new MemoryCacheManager();
    }
}
