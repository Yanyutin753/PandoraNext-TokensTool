package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.systemServiceImpl;
import com.tokensTool.pandoraNext.util.MyTaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class taskController {

    private final MyTaskUtils myTaskUtils;

    @Autowired
    private systemServiceImpl systemSetting;
    @Autowired
    public taskController(MyTaskUtils myTaskUtils) {
        this.myTaskUtils = myTaskUtils;
    }

    @PostMapping("/controllerTask")
    public Result controllerTask(@RequestBody systemSetting setting) {
        try {
            if(! setting.getAuto_updateSession()){
                myTaskUtils.stopTask();
            }
            else{
                myTaskUtils.changeTask(setting);
            }
            String res = systemSetting.requireTimeTask(setting);
            if(res.contains("成功")){
                return Result.success(res);
            }
            return Result.error(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("修改自动更新任务和网页失败！");
    }

}
