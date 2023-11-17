package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yangyang
 * @create 2023-11-11 18:19
 */
@RestController
@RequestMapping("/api")
public class autoTokenController {
    @Autowired
    private com.yyandywt99.pandoraNext.service.apiService apiService;

    /**
     * 自动更新Token
     * 更换fakeApiTool里存储的Token
     * 更换One-API相应的FakeAPI
     * @return "更新成功" or "更新失败"
     * @throws Exception
     */
    @Scheduled(cron = "0 3 0 */5 * ?")
    public Result toUpdateToken(){
        try {
            String res = apiService.autoUpdateToken("");
            if(res.contains("自动修改Token成功")){
                return Result.success(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("更新失败");
    }

    @PostMapping ("updateToken")
    public Result toUpdateToken(@RequestBody token token){
        try {
            String res = apiService.autoUpdateSimpleToken(token);
            if(res != null && res.length() > 300){
                return Result.success(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新Token失败,请尝试重新登录！");
    }
}
