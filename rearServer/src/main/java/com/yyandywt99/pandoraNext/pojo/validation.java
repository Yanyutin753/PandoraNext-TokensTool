package com.yyandywt99.pandoraNext.pojo;

/**
 * @author Yangyang
 * @create 2023-11-27 18:40
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户验证码验证的配置类。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class validation {

    /**
     *验证码提供商
     */
    private String provider;
    /**
     *验证码供应商后台获取的网站参数，是可以公布的信息
     */
    private String site_key;
    /**
     *验证码供应商后台获取的秘密参数，不要公布出来。有些供应商也称作API Key
     */
    private String site_secret;

    /**
     *在全站密码登录页面显示
     */
    private boolean site_login;
    /**
     *在设置登录页面显示
     */
    private boolean setup_login;
    /**
     *在输入用户名页面显示
     */
    private boolean oai_username;
    /**
     *在输入密码页面显示
     */
    private boolean oai_password;

}

