package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yangyang
 * @create 2024-01-08 10:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class shareToken {
    /**
     * 接入oneApi的独一无二的名字
     */
    private String oneApi_name;
    /**
     * 接入oneApi的token名
     * 拿share
     */
    private String token_name;
    /**
     * 接入oneApi的token名
     * 拿share
     */
    private String token_value;

    /**
     * 接入oneApi自定义PandoraNext地址
     */
    private String oneApi_baseUrl;

    /**
     * 接入oneApi自定义PandoraNext模型
     */
    private String oneApi_models;

    /**
     * 接入oneApi的组别
     */
    private String oneApi_groups;

    /**
     * 接入oneApi的优先级
     */
    private Integer priority;

    /**
     * pool_token 更新时间
     */
    private String shareTime;
}
