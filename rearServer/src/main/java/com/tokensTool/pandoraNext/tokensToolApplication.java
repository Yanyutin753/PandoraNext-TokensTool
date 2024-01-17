package com.tokensTool.pandoraNext;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.apiServiceImpl;
import com.tokensTool.pandoraNext.service.impl.poolServiceImpl;
import com.tokensTool.pandoraNext.service.impl.systemServiceImpl;
import com.tokensTool.pandoraNext.util.JwtUtils;
import com.tokensTool.pandoraNext.util.MyTaskUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;
import java.util.UUID;

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

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private apiServiceImpl serviceImpl;
    @Autowired
    private systemServiceImpl systemService;
    @Autowired
    private poolServiceImpl poolServiceImpl;
    @Autowired
    private MyTaskUtils myTaskUtils;

    public static void main(String[] args) {
        log.info("\n--------------------------------------------------------------\n" +
                "PandoraNext-tokensTool v 0.6.7版本\n" +
                "1.优化添加进one-api的代码，使得id不一直增加\n" +
                "2.优化copilot，cocopilot的chat，embeddings接口，接受高并发\n" +
                "3.修复历史问题bug,优化代码，优化前端\n" +
                "--------------------------------------------------------------\n");
        // 启动
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        log.info("设置专属的signKey成功！");
        // 初始化UUID
        String uuidString = UUID.randomUUID().toString();
        JwtUtils.setJwtPassword(uuidString);
        log.info("初始化UUID成功！");
        SpringApplication.run(tokensToolApplication.class, args);
    }

    @PostConstruct
    public void initialize() {
        //初始化检查sessionToken
        serviceImpl.initializeCheckSession();
        //初始化检查config.json
        systemService.initializeConfigJson();
        //初始化检查token.json
        serviceImpl.initializeTokenJson();
        //初始化定时任务
        poolServiceImpl.initializeCheckPool();
        systemSetting setting = systemService.selectSetting();
        if (!setting.getAuto_updateSession()) {
            myTaskUtils.stopTask();
        } else {
            myTaskUtils.changeTask(setting);
        }

        log.info("------------------------------------------------------");
        log.info("----------原神PandoraNext-tokensTool启动成功------------");
        log.info("初始用户名：root" );
        log.info("初始密码：123456" );
        log.info("访问地址：http://0.0.0.0:" + serverPort + contextPath +"");
        log.info("------------------------------------------------------");
    }
}

