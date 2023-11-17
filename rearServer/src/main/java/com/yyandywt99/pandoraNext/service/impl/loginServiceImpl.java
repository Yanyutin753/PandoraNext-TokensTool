package com.yyandywt99.pandoraNext.service.impl;

import com.yyandywt99.pandoraNext.service.loginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Yangyang
 * @create 2023-11-17 14:15
 */
@Slf4j
@Service
public class loginServiceImpl implements loginService {
    @Value("${loginUsername}")
    private String loginUsername;

    @Value("${loginPassword}")
    private String loginPassword;

    /**
     * 新增保存登录信息
     * 通过jar包可以实现修改密码，修改数据
     * @return "登录成功！"
     * "用户名账号错误"
     */
    @Override
    public String login(String userName, String password) {
        try {
            if(userName.equals(loginUsername) && password.equals(loginPassword)){
                return "登录成功！";
            }
            else {
                return "用户名账号错误";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "用户名账号或密码错误";
        }
    }
}
