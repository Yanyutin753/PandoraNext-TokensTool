package com.tokensTool.pandoraNext.aop;

import com.tokensTool.pandoraNext.controller.apiController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Yangyang
 * @create 2023-10-07 13:35
 */
@Slf4j
@Component
//切面类
@Aspect
public class LogAspect {
    /**
     * @拿到controller里的reload()方法
     */
    private apiController controller;
    /**
     * @是否开启热重载
     */
    @Value("${hotReload}")
    private String hotReload;

    @Autowired
    public void setController(apiController controller) {
        this.controller = controller;
    }

    /**
     * @在方法之后执行reload
     */
    @After("@annotation(com.tokensTool.pandoraNext.anno.Log)")
    public void recordLog() {
        if (Boolean.parseBoolean(hotReload)) {
            try {
                log.info(controller.reloadContainer().toString());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("热重载失败！");
            }
        } else {
            log.error("热重载未开启！");
        }
    }
}
