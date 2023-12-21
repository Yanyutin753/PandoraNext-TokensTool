package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.poolToken;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.impl.apiServiceImpl;
import com.tokensTool.pandoraNext.service.impl.poolServiceImpl;
import com.tokensTool.pandoraNext.service.impl.systemServiceImpl;
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

    @Autowired
    private poolServiceImpl poolService;

    /**
     * 获取全部share_tokens
     * 返回share_tokens或Not_Login
     */
    @GetMapping("/shared_token")
    public Result getSharedToken(@RequestParam("password") String password) {
        List<String> res = new ArrayList<>();
        systemSetting systemSetting = systemService.selectSetting();
        if (!systemSetting.getIsGetToken()) {
            return Result.error("Not_Open");
        }
        if (password.equals(systemSetting.getGetTokenPassword())) {
            for (token token : apiService.selectToken("")) {
                res.add(token.getShare_token());
            }
            return Result.success(res);
        } else {
            return Result.error("Not_Login");
        }
    }

    /**
     * 获取指定share_token
     * 返回share_token或Not_Login
     */
    @GetMapping("/token/shared_token")
    public Result getSimplySharedToken(@RequestParam("password") String password,
                                       @RequestParam("tokenName") String tokenName) {
        systemSetting systemSetting = systemService.selectSetting();
        if (!systemSetting.getIsGetToken()) {
            return Result.error("Not_Open");
        }
        if (password.equals(systemSetting.getGetTokenPassword())) {
            for (token token : apiService.selectToken("")) {
                if (token.getName().equals(tokenName)) {
                    if (token.getShare_token() != null) {
                        return Result.success(token.getShare_token());
                    }
                    return Result.error("该tokenName没有存放Shared_Token");
                }
            }
            return Result.error("未找到该tokenName！");
        } else {
            return Result.error("Not_Login");
        }

    }

    /**
     * 获取全部access_token
     * 返回access_token或Not_Login
     */
    @GetMapping("/access_token")
    public Result getAccessToken(@RequestParam("password") String password) {
        List<String> res = new ArrayList<>();
        systemSetting systemSetting = systemService.selectSetting();
        if (!systemSetting.getIsGetToken()) {
            return Result.error("Not_Open");
        }
        if (password.equals(systemSetting.getGetTokenPassword())) {
            for (token token : apiService.selectToken("")) {
                res.add(token.getAccess_token());
            }
            return Result.success(res);
        } else {
            return Result.error("Not_Login");
        }
    }

    /**
     * 获取指定access_token
     * 返回access_token或Not_Login
     */
    @GetMapping("/token/access_token")
    public Result getSimplyAccessToken(@RequestParam("password") String password,
                                       @RequestParam("tokenName") String tokenName) {
        systemSetting systemSetting = systemService.selectSetting();
        if (!systemSetting.getIsGetToken()) {
            return Result.error("Not_Open");
        }
        if (password.equals(systemSetting.getGetTokenPassword())) {
            for (token token : apiService.selectToken("")) {
                if (token.getName().equals(tokenName)) {
                    if (token.getAccess_token() != null) {
                        return Result.success(token.getAccess_token());
                    }
                    return Result.error("该tokenName没有存放Access_Token");
                }
            }
            return Result.error("未找到该tokenName！");
        } else {
            return Result.error("Not_Login");
        }
    }


    /**
     * 获取单个pool_token
     * 返回pool_token或Not_Login
     */
    @GetMapping("/token/pool_token")
    public Result getSimplePoolToken(@RequestParam("password") String password,
                                     @RequestParam("tokenName") String tokenName) {
        systemSetting systemSetting = systemService.selectSetting();
        if (!systemSetting.getIsGetToken()) {
            return Result.error("Not_Open");
        }
        if (password.equals(systemSetting.getGetTokenPassword())) {
            try {
                List<poolToken> poolTokens = poolService.selectPoolToken("");
                for (poolToken poolToken : poolTokens) {
                    if (poolToken.getPoolName().equals(tokenName)) {
                        String poolValue = poolToken.getPoolToken();
                        if (poolValue != null && poolValue.contains("pk")) {
                            return Result.success(poolValue);
                        }
                    }
                }
                return Result.error("该tokensTool没有正确生成相应名称的pool_Token");
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取pool_token出现问题！");
            }
        }
        return Result.error("Not_Login");
    }
}
