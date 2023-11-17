package com.yyandywt99.pandoraNext;

import com.yyandywt99.pandoraNext.config.TaskSchedulerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author YANGYANG
 */
//定时注解2
@Import(TaskSchedulerConfig.class)
//定时注解
@EnableScheduling
@SpringBootApplication
public class FakeApiToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakeApiToolApplication.class, args);
    }

}
