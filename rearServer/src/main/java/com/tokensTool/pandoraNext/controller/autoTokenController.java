package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.anno.Log;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.impl.poolServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yangyang
 * @create 2023-11-11 18:19
 */

@Slf4j
@RestController
@RequestMapping("/api")
public class autoTokenController {


    @Autowired
    private com.tokensTool.pandoraNext.service.apiService apiService;

    @Autowired
    private poolServiceImpl poolService;

    /**
     * 自动更新access_Token和share_token
     * 更换tokens.json里存储的Tokens
     * 自动重启
     * @return "更新成功" or "更新失败"
     * @throws Exception
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public Result toUpdateToken(){
        try {
            log.info("开始自动更新Token..........................");
            String res = apiService.autoUpdateToken("");
            if(res.contains("修改Token成功")){
                try {
                    return Result.success(res);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("更新失败");
    }

    @GetMapping("updateAllToken")
    public Result toUpdateAllToken(){
        try {
            String res = apiService.autoUpdateToken("");
            if(res.contains("生成Token成功")){
                try {
                    String s = poolService.refreshAllTokens();
                    return Result.success(res + "\n" + s);
                } catch (Exception e) {
                    return Result.success(res +",但是自动更新pool_token失败，请手动点击一键全更新pool_token!");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return Result.error("生成access_token和share_token失败");
    }


    /**
     * 自动更新指定用户名的access_token和share_token
     * @return "更新成功" or "刷新Token失败,请尝重新刷新！”
     * @throws Exception
     */
    @PostMapping ("updateToken")
    public Result toUpdateToken(@RequestBody token token){
        try {
            token res = apiService.autoUpdateSimpleToken(token);
            if(res != null){
                return Result.success(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("生成access_token和share_token失败,请请确保填写token为session_token！");
    }

    /**
     * 自动更新指定用户名的session
     * @return "更新成功" or "刷新Token失败,请尝重新刷新！”
     * @throws Exception
     */
    @Log
    @PostMapping ("updateSessionToken")
    public Result toUpdateSessionToken(@RequestBody token token){
        try {
            String res = apiService.autoUpdateSessionToken(token);
            if(res != null && res.length() > 300){
                token.setToken(res);
                token.setSetPoolToken(true);
                Result update = toUpdateToken(token);
                if(update.getCode() == 1){
                    return Result.success(res);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新session_token失败,请尝重新刷新！");
    }

}
