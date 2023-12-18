package com.tokensTool.pandoraNext;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.apiServiceImpl;
import com.tokensTool.pandoraNext.service.impl.systemServiceImpl;
import com.tokensTool.pandoraNext.util.JwtUtils;
import com.tokensTool.pandoraNext.util.MyTaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
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

    @Autowired
    private apiServiceImpl serviceImpl;

    @Autowired
    private systemServiceImpl systemService;

    @Autowired
    private MyTaskUtils myTaskUtils;

    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.4.9.2版本，增加自定义刷新session_token，" +
                "每天凌晨2点自动更新access_token,share_token,pool_token," +
                "并检查session_token,并标记过期session_token");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        SpringApplication.run(tokensToolApplication.class, args);
    }

    @PostConstruct
    public void initialize() {
        //初始化检查sessionToken
        serviceImpl.initializeCheckSession();
        //初始化检查config.json
        systemService.initializeConfigJson();
        //初始化定时任务
        systemSetting setting = systemService.selectSetting();
        if(! setting.getAuto_updateSession()){
            myTaskUtils.stopTask();
        }
        else{
            myTaskUtils.changeTask(setting);
        }
    }
}

