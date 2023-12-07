package com.yyandywt99.pandoraNext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author YANGYANG
 */

/**
 * 定时注解
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class tokensToolApplication {
    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.4.7.1 版本，修改了jwt的漏洞问题，感谢您的使用！");
        SpringApplication.run(tokensToolApplication.class, args);
    }

}
