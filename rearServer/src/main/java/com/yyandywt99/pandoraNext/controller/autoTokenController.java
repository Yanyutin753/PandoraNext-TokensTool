package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.anno.Log;
import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.token;
import com.yyandywt99.pandoraNext.service.systemService;
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

    private static String poolToken = "";

    @Autowired
    private com.yyandywt99.pandoraNext.service.apiService apiService;

    @Autowired
    private systemService systemService;

    public static String getPoolToken() {
        return poolToken;
    }

    public static void setPoolToken(String res){
        poolToken = res;
    }
    /**
     * 自动更新access_Token和share_token
     * 更换tokens.json里存储的Tokens
     * 自动重启
     * @return "更新成功" or "更新失败"
     * @throws Exception
     */
    @Scheduled(cron = "0 3 0 */5 * ?")
    public Result toUpdateToken(){
        try {
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

    @GetMapping("updatePoolToken")
    public Result toUpdatePoolToken(){
        try {
            String temPoolToken = autoTokenController.getPoolToken();
            log.info(temPoolToken);
            String res = apiService.toUpdatePoolToken(temPoolToken);
            if(! res.equals(temPoolToken)){
                setPoolToken(res);
                log.info(poolToken);
            }
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新pool_token失败,请尝重新刷新！");
    }

    @GetMapping("changePoolToken")
    public Result toChangePoolToken(){
        try {
            String temPoolToken = "";
            String res = apiService.toUpdatePoolToken(temPoolToken);
            if(! res.equals(temPoolToken)){
                setPoolToken(res);
                log.info(poolToken);
            }
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("更换pool_token失败,请尝重新更换！");
    }

    @Scheduled(cron = "0 4 0 */5 * ?")
    public void  autoUpdatePoolToken(){
        try {
            String temPoolToken = autoTokenController.getPoolToken();
            String res = apiService.toUpdatePoolToken(temPoolToken);
            if(! res.equals(temPoolToken)){
                setPoolToken(res);
                log.info("pool token已更新为：" + temPoolToken);
            }
            log.info("已成功更新pool_token，pool_token保持不变");
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("更新pool_token失败！");
    }
}
