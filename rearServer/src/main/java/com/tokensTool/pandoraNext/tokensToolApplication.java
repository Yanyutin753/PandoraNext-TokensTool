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
        log.info("PandoraNext-tokensTool v 0.4.9.1版本，增加PandoraNext余额查询，优化前端，新增token复制功能");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        SpringApplication.run(tokensToolApplication.class, args);
    }
}
