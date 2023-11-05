package com.snw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableCaching
public class App {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
        run.getBean(CustomService.class).repeatableMethod();
        run.getBean(CustomService.class).repeatableMethod();
        run.getBean(CustomService.class).evict();
        run.getBean(CustomService.class).repeatableMethod();
    }
}
