package com.tokensTool.pandoraNext;

import com.tokensTool.pandoraNext.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;

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
        log.info("PandoraNext-tokensTool v 0.4.7.3 版本，增加在线开关tokenTools接口,优化前端代码，增加注释");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        SpringApplication.run(tokensToolApplication.class, args);
    }
}
