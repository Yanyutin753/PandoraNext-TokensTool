package com.tokensTool.pandoraNext.service.impl;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.loginService;
import com.tokensTool.pandoraNext.service.systemService;
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
    public boolean login(systemSetting setting) {
        try {
            systemSetting systemSetting = systemService.selectSetting();
            String loginUsername = systemSetting.getLoginUsername();
            String loginPassword = systemSetting.getLoginPassword();
            if(setting.getLoginUsername().equals(loginUsername) && setting.getLoginPassword().equals(loginPassword)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
