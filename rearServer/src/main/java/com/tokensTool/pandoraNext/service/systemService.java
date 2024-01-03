package com.tokensTool.pandoraNext.service;

import com.tokensTool.pandoraNext.pojo.systemSetting;

/**
 * @author Yangyang
 * @create 2023-11-18 9:54
 */
public interface systemService {
    /**
     * 修改config.json里的系统值
     *
     * @return "修改成功！"or"修改失败"
     */
    String requiredSetting(systemSetting tem);

    /**
     * 查询config.json里的系统值
     *
     * @return systemSettings类
     */
    systemSetting selectSetting();

    /**
     * 查询config.json里的baseUrl
     *
     * @return systemSettings类
     */
    systemSetting selectSettingUrl();

}
