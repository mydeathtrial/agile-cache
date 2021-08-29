package cloud.agileframework.cache.config;

import cloud.agileframework.cache.util.BeanUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * @author 佟盟
 * 日期 2020/7/30 15:17
 * 描述 启动监听
 * @version 1.0
 * @since 1.0
 */
public class SpringBootApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private final SpringApplication application;

    public SpringBootApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
    }


    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        BeanUtil.setApplicationContext(context);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
