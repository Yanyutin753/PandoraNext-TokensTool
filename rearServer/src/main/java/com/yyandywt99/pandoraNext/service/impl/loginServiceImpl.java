package com.yyandywt99.pandoraNext.service.impl;

import com.yyandywt99.pandoraNext.pojo.systemSetting;
import com.yyandywt99.pandoraNext.service.loginService;
import com.yyandywt99.pandoraNext.service.systemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yangyang
 * @create 2023-11-17 14:15
 */
@Slf4j
@Service
public class loginServiceImpl implements loginService {

    /**
     * systemService从这里面
     * 拿到两个变量
     * loginUsername
     * loginPassword
     * 返回登录成功！或用户名账号错误"
     */
    @Autowired
    private systemService systemService;


    /**
     * 新增保存登录信息
     * 通过config.json文件可以实现修改密码，修改数据
     * @return "登录成功！"
     * "用户名账号错误"
     */
    @Override
    public String login(systemSetting setting) {
        try {
            systemSetting systemSetting = systemService.selectSetting();
            String loginUsername = systemSetting.getLoginUsername();
            String loginPassword = systemSetting.getLoginPassword();
            if(setting.getLoginUsername().equals(loginUsername) && setting.getLoginPassword().equals(loginPassword)){
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
