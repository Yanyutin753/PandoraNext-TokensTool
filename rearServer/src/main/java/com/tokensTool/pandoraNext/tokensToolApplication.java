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
        log.info("PandoraNext-tokensTool v 0.4.8.2版本，增加创建多pool_tokens，优化自动刷新过程，修改前端电脑端bug");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        SpringApplication.run(tokensToolApplication.class, args);
    }
}
