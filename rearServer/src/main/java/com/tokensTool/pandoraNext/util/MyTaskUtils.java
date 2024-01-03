package com.tokensTool.pandoraNext.util;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.apiServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class MyTaskUtils {
    private final TaskScheduler taskScheduler;
    @Autowired
    private apiServiceImpl apiService;
    private ScheduledFuture<?> future;

    public MyTaskUtils(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }


    public void stopTask() {
        if (future != null) {
            future.cancel(true);
            log.info("取消定时任务成功！");
        }
    }

    public void changeTask(systemSetting setting) {
        try {
            if (future != null) {
                future.cancel(true);
            }
            String cron = "0 0 1 */" + setting.getAuto_updateTime() + " * ?";
            future = taskScheduler.schedule(() -> {
                // 这里是任务内容
                System.out.println("自定义定时更新session_token开始进行...................");
                for (int i = 0; i < setting.getAuto_updateNumber(); i++) {
                    apiService.updateSession();
                    log.info("更新session_token开始完成");
                }
            }, new CronTrigger(cron));
            log.info("定时任务开启成功！");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
