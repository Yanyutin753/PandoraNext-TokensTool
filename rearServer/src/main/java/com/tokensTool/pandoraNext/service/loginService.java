package com.tokensTool.pandoraNext.service;

import com.tokensTool.pandoraNext.pojo.systemSetting;

/**
 * @author Yangyang
 * @create 2023-11-17 14:14
 */

public interface loginService {

    /**
     * 新增保存登录信息
     * 通过config.json文件可以实现修改密码，修改数据
     * @return "登录成功！"
     * "用户名账号错误"
     */

    boolean login(systemSetting setting);
}
