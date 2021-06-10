package cloud.agileframework.cache.support.redis;

import com.fasterxml.jackson.databind.Module;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-06-09 16:27
 * 描述 用于获取jackson2序列化配置，开发者可以自定义Module进行注入
 * @version 1.0
 * @since 1.0
 */
public interface Jackson2ModuleProvider {
    /**
     * 注入多个模块
     */
    default List<Module> modules() {
        return Lists.newArrayList();
    }

    /**
     * 注入一个
     */
    default Module module() {
        return null;
    }
}
