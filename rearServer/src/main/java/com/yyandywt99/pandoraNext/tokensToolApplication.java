package com.yyandywt99.pandoraNext;

import com.yyandywt99.pandoraNext.config.TaskSchedulerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author YANGYANG
 */

/**
 * 定时注解
 */
@Import(TaskSchedulerConfig.class)
/**
 * 定时注解
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class tokensToolApplication {

    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.4.4 版本，感谢您的使用！");
        SpringApplication.run(tokensToolApplication.class, args);
    }

}
