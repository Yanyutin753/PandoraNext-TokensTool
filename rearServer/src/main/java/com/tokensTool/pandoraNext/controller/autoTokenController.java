package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.anno.Log;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.impl.poolServiceImpl;
import com.tokensTool.pandoraNext.service.impl.shareServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private shareServiceImpl shareService;

    /**
     * 自动更新access_Token和share_token
     * 更换tokens.json里存储的Tokens
     * 自动重启
     *
     * @return "更新成功" or "更新失败"
     * @throws Exception
     */
    @Log
    @Scheduled(cron = "0 0 */6 * * ?")
    public void toUpdateToken() {
        try {
            log.info("开始自动检查更新refresh_token,session_token,生成和刷新share_token,pool_token..........................");
            toUpdateAllToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.warn(Result.error("自动检查更新access_token,share_token和pool_token失败").toString());
    }

    /**
     * 一键检查所有session或者refresh_token
     * 失效变黄
     * 并更新所有access_token和share_token
     * 并重新组成pool_token
     *
     * @return
     */
    @Log
    @GetMapping("updateAllToken")
    public Result toUpdateAllToken() {
        try {
            String res = apiService.autoUpdateToken("");
            if (res.contains("生成Token成功")) {
                try {
                    String s = poolService.refreshAllPoolTokens();
                    String s1 = shareService.refreshAllShareTokens();
                    return Result.success(res + s + s1);
                } catch (Exception e) {
                    return Result.success(res + "<br>但是自动更新pool_token和oneApi里的share_token失败");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return Result.error("生成access_token和share_token失败");
    }


    /**
     * 自动更新指定用户名的access_token和share_token
     *
     * @return "更新成功" or "刷新Token失败,请尝重新刷新！”
     * @throws Exception
     */
    @PostMapping("updateToken")
    public Result toUpdateToken(@RequestBody token token) {
        try {
            token res = apiService.autoUpdateSimpleToken(token);
            if (res != null) {
                return Result.success(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("生成access_token和share_token失败,请请确保填写token为session或refresh_token！");
    }

    /**
     * 自动更新指定用户名的session或refresh
     *
     * @return "更新成功" or "刷新Token失败,请尝重新刷新！”
     * @throws Exception
     */
    @Log
    @PostMapping("updateSessionToken")
    public Result toUpdateSessionToken(@RequestBody token token) {
        try {
            token resToken = apiService.updateSession(token);
            if (resToken != null) {
                return Result.success("刷新成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新失败,请尝重新刷新或检查proxy的url填写是否正确！");
    }

    /**
     * 自动更新指定用户名的session或refresh组
     *
     * @return "更新成功" or "刷新Token失败,请尝重新刷新！”
     * @throws Exception
     */
    @Log
    @PostMapping(value = "/updateSessionTokenList", consumes = "application/json")
    public Result toUpdateSessionTokenList(@RequestBody List<token> tokens) {
        try {
            int count = 0;
            for (token token : tokens) {
                if (token.isSetPoolToken()) {
                    token resToken = apiService.updateSession(token);
                    if (resToken != null) {
                        count++;
                    }
                } else {
                    count++;
                }
            }
            return Result.success("刷新成功：" + count + "，失败：" + (tokens.size() - count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新失败,请尝重新刷新或检查proxy的url填写是否正确！");
    }
}
