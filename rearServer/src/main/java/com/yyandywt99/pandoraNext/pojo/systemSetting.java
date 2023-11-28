package com.yyandywt99.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yangyang
 * @create 2023-11-18 10:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class systemSetting {
    /**
     * 模式类型
     */
    private String server_mode;
    /**
     * 绑定IP和端口
     */
    private String bing;
    /**
     * 请求的超时时间
     */
    private Integer timeout;
    /**
     * 部署服务流量走代理
     */
    private String proxy_url;
    /**
     * GPT中创建的对话分享
     */
    private Boolean public_share;
    /**
     * 访问网站密码
     */
    private String site_password;
    /**
     * 重载服务密码
     */
    private String setup_password;
    /**
     * 白名单（null则不限制，为空数组[]则限制所有账号）
     */
    private String whitelist;

    /**
     * pandoraNext验证license_id
     */
    private String license_id;

    /**
     * tokensTool登录Username
     */
    private String loginUsername;

    /**
     * tokensTool密码Password
     */
    private String loginPassword;


    /**
     * tokensTool 验证信息
     */
    private validation validation;

    /**
     * tokensTool 更新token网址
     * 为"default"则调用本机的，不为“default"则自定义
     */
    private String autoToken_url;


    /**
     * tokensTool 更新token的类型
     * 为"access_token"则拿access_token，为“session_token"则拿session_token
     * 默认拿access_token
     */
    private String tokenKind;
//    /**
//     * tokensTool是否自动验证PandoraNext
//     */
//    private Boolean verifyEnabled;
//
//    /**
//     * tokensTool自动验证时间
//     */
//    private Integer verify_time;
}
