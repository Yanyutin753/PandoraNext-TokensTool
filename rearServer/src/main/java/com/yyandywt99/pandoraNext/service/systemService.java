package com.yyandywt99.pandoraNext.service;

import com.yyandywt99.pandoraNext.pojo.systemSetting;

/**
 * @author Yangyang
 * @create 2023-11-18 9:54
 */
public interface systemService {
    String requiredSetting(systemSetting tem);
    systemSetting selectSetting();

    String requiredPoolToken(systemSetting setting);
}
