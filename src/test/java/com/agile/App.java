package com.agile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 佟盟
 * 日期 2020/7/14 17:56
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        new SpringApplication(App.class).run(args);
    }
}
