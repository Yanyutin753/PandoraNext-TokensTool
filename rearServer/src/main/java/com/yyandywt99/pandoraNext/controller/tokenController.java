package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.token;
import com.yyandywt99.pandoraNext.service.impl.apiServiceImpl;
import com.yyandywt99.pandoraNext.service.impl.systemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-29 10:22
 */
@RestController
public class tokenController {

    @Autowired
    private systemServiceImpl systemService;

    @Autowired
    private apiServiceImpl apiService;

    @GetMapping("/shared_token")
    public Result getSharedToken(@RequestParam("password") String password){
        List<String> res = new ArrayList<>();
        if(password.equals(systemService.selectSetting().getGetTokenPassword())){
            for (token token : apiService.seleteToken("")) {
                res.add(token.getShare_token());
            }
            return Result.success(res);
        }
        else{
            return Result.error("Not_Login");
        }
    }
    @GetMapping("/token/shared_token")
    public Result getSimplySharedToken(@RequestParam("password") String password,
                                       @RequestParam("tokenName") String tokenName){
        List<String> res = new ArrayList<>();
        if(password.equals(systemService.selectSetting().getGetTokenPassword())){
            for (token token : apiService.seleteToken("")) {
                if(token.getName().equals(tokenName)){
                    if(token.getShare_token() != null){
                        return Result.success(token.getShare_token());
                    }
                    return Result.error("该tokenName没有存放Shared_Token");
                }
                res.add(token.getShare_token());
            }
            return Result.error("未找到该tokenName！");
        }
        else{
            return Result.error("Not_Login");
        }

    }

    @GetMapping("/pool_token")
    public Result getPoolToken(@RequestParam("password") String password){
        if(password.equals(systemService.selectSetting().getGetTokenPassword())) {
            try {
                String poolToken = autoTokenController.getPoolToken();
                if(poolToken != null && poolToken.contains("pk")){
                    return Result.success(autoTokenController.getPoolToken());
                }
                else{
                    return Result.error("该tokensTool没有正确生成pool_Token");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取pool_token出现问题！");
            }
        }
        return Result.error("Not_Login");
    }
}
