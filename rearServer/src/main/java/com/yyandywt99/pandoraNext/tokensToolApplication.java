package com.yyandywt99.pandoraNext;

import com.yyandywt99.pandoraNext.util.JwtUtils;
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
        log.info("PandoraNext-tokensTool v 0.4.7.2 版本，修改了jwt的漏洞问题，新增时间戳来加强防御，感谢您的使用！");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        SpringApplication.run(tokensToolApplication.class, args);
    }
}
