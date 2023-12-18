package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class token {

    /**
     * token 名称
     */
    private String name;

    /**
     * token 值
     */
    private String token;

    /**
     * token openai账号用户名
     */
    private String username;

    /**
     * token openai账号用户密码
     */
    private String userPassword;

    /**
     * token 是否分享
     */
    private boolean shared;

    /**
     * token 是否分享聊天信息
     */
    private boolean show_user_info;

    /**
     * token 是否显示金光
     */
    private boolean plus;

    /**
     * token 是否合成pool_token
     */
    private boolean setPoolToken;

    /**
     * token 进去token的密码
     */
    private String password;

    /**
     * token share_token的值
     */
    private String share_token;

    /**
     * token access_token的值
     */
    private String access_token;

    /**
     * token session_token获取的时间
     */
    private String updateTime;

    /**
     * token 检查session是否过期
     */
    private boolean checkSession;
}