package com.yyandywt99.pandoraNext;

<<<<<<< HEAD

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
=======
import com.yyandywt99.pandoraNext.config.TaskSchedulerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author YANGYANG
 */

/**
 * 定时注解
 */
<<<<<<< HEAD


=======
@Import(TaskSchedulerConfig.class)
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
/**
 * 定时注解
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class tokensToolApplication {
<<<<<<< HEAD
    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.4.7.1 版本，修改了jwt的漏洞问题，感谢您的使用！");
=======

    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.4.7 版本，感谢您的使用！");
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
        SpringApplication.run(tokensToolApplication.class, args);
    }

}
