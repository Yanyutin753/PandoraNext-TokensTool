package com.tokensTool.pandoraNext;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.apiServiceImpl;
import com.tokensTool.pandoraNext.service.impl.poolServiceImpl;
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

    @Autowired
    private apiServiceImpl serviceImpl;

    @Autowired
    private systemServiceImpl systemService;

    @Autowired
    private poolServiceImpl poolServiceImpl;

    @Autowired
    private MyTaskUtils myTaskUtils;

    public static void main(String[] args) {
        log.info("PandoraNext-tokensTool v 0.5.1版本\n" +
                "新增将pool_token传入one-api\n" +
                "修复相关Web转Api修改，添加，刷新token的bug\n" +
                "页面优化，更加直观");
        Instant instant = Instant.now();
        String key = String.valueOf(instant.toEpochMilli());
        JwtUtils.setSignKey(key);
        log.info("设置专属的signKey成功！");
        // 初始化UUID
        String uuidString = UUID.randomUUID().toString();
        JwtUtils.setJwtPassword(uuidString);
        log.info("初始化UUID成功！");
        // 启动
        SpringApplication.run(tokensToolApplication.class, args);
        log.info("原神tokensTool启动！！！！！！！！！！！！！！！");
    }

    @PostConstruct
    public void initialize() {
        //初始化检查sessionToken
        serviceImpl.initializeCheckSession();
        //初始化检查config.json
        systemService.initializeConfigJson();
        //初始化定时任务
        poolServiceImpl.initializeCheckPool();

        systemSetting setting = systemService.selectSetting();
        if(! setting.getAuto_updateSession()){
            myTaskUtils.stopTask();
        }
        else{
            myTaskUtils.changeTask(setting);
        }
    }
}

