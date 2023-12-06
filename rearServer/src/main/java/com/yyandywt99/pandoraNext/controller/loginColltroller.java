package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.systemSetting;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
import com.yyandywt99.pandoraNext.service.impl.systemServiceImpl;
import com.yyandywt99.pandoraNext.service.loginService;
import com.yyandywt99.pandoraNext.util.JwtUtils;
import io.jsonwebtoken.Claims;
<<<<<<< HEAD
=======
=======
import com.yyandywt99.pandoraNext.service.loginService;
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yangyang
 * @create 2023-11-17 14:15
 */
@Slf4j
@RestController()
@RequestMapping("/api")
public class loginColltroller {
    @Autowired
    private loginService loginService;
<<<<<<< HEAD

    @Autowired
    private systemServiceImpl systemService;
=======
<<<<<<< HEAD

    @Autowired
    private systemServiceImpl systemService;
=======
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
    /**
     * 登录用户接口
     * @return "jwt令牌！"or"NOT_LOGIN"
     */
    @PostMapping("/login")
    public Result login(@RequestBody systemSetting setting) {
        String res = loginService.login(setting);
<<<<<<< HEAD
        log.info(res);
=======
<<<<<<< HEAD
        log.info(res);
        if(res.contains("登录成功")){
            String password = setting.getLoginPassword();
            JwtUtils.setSignKey(password);
            log.info("登录成功");
            Map<String,Object> chaims = new HashMap<String,Object>();
            chaims.put("password",password);
            String s = JwtUtils.generateJwt(chaims);
=======
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
        if(res.contains("登录成功")){
            String password = setting.getLoginPassword();
            JwtUtils.setSignKey(password);
            log.info("登录成功");
            Map<String,Object> chaims = new HashMap<String,Object>();
<<<<<<< HEAD
            chaims.put("password",password);
            String s = JwtUtils.generateJwt(chaims);
=======
            chaims.put("id",1);
            String s = com.yyandywt99.pandoraNext.util.JwtUtils.generateJwt(chaims);
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
            return Result.success(s);
        }
        return Result.error("登陆失败");
    }

    /**
     * 验证是否登录成功
     * @return 没登陆成功否则返回"NOT_LOGIN"
     */
    @PostMapping("/loginToken")
    public Result loginToken(@RequestParam("token") String token){
<<<<<<< HEAD
        String password = systemService.selectSetting().getLoginPassword();
        JwtUtils.setSignKey(password);
=======
<<<<<<< HEAD
        String password = systemService.selectSetting().getLoginPassword();
        JwtUtils.setSignKey(password);
=======
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
        log.info(token);
        if(!StringUtils.hasLength(token)){
            log.info("请求头token为空,返回未登录的信息");
            return Result.error("NOT_LOGIN");
        }
        try {
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
            Claims claims = JwtUtils.parseJWT(token);
            String res = claims.get("password").toString();
            if(res.equals(password)){
                log.info("令牌合法，可以正常登录");
                return Result.success("YES_LOGIN");
            }
<<<<<<< HEAD
=======
=======
            com.yyandywt99.pandoraNext.util.JwtUtils.parseJWT(token);
            log.info("令牌合法，可以正常登录");
            return Result.success("YES_LOGIN");
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
        } catch (Exception e) {//jwt解析失败
            e.printStackTrace();
            log.info("解析令牌失败, 返回未登录错误信息");
            Result error = Result.error("NOT_LOGIN");
            return error;
        }
<<<<<<< HEAD
        Result error = Result.error("NOT_LOGIN");
        return error;
=======
<<<<<<< HEAD
        Result error = Result.error("NOT_LOGIN");
        return error;
=======
>>>>>>> bcd58edf7697081bd86d12c983b1afcac8db4495
>>>>>>> a01050d85eea9b18e5166217a20b6fd0187faa2e
    }
}
