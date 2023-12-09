package com.tokensTool.pandoraNext.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Data
public class TimerScheduleTask implements SchedulingConfigurer {

    /**
     * @lazy 防止依赖循环
     */
    @Lazy
    @Autowired
    private com.tokensTool.pandoraNext.controller.apiController apiController;

    /**
     * 默认时间60（分钟）
     */
    private Integer timer = 60;

    /**
     * 默认true
     */
    private boolean enabled = false;

    private ScheduledTaskRegistrar taskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;

        // 只有在 enabled 为 true 时才添加定时任务
        taskRegistrar.addTriggerTask(
                // Runnable实现
                () -> {
                    if (enabled) {
                        log.info("正在自动验证jwt中......................................................");
//                        Result result = apiController.verifyContainer();
//                        log.info(result.toString());
                    }
                },
                // Trigger实现
                triggerContext -> {
                    PeriodicTrigger periodicTrigger = new PeriodicTrigger(timer * 60000);
                    return periodicTrigger.nextExecutionTime(triggerContext);
                }
        );
    }
}



