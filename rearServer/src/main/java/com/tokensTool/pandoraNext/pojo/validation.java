package com.tokensTool.pandoraNext.pojo;

/**
 * @author Yangyang
 * @create 2023-11-27 18:40
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

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


    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("provider", this.getProvider());
        json.put("site_key", this.getSite_key());
        json.put("site_secret", this.getSite_secret());
        json.put("site_login", this.isSite_login());
        json.put("setup_login", this.isSetup_login());
        json.put("oai_username", this.isOai_username());
        json.put("oai_password", this.isOai_password());
        return json;
    }
}

