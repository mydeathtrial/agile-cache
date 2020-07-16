package com.agile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 佟盟
 * 日期 2020/7/14 17:56
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@EnableCaching
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        try {
            new SpringApplication(App.class).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
