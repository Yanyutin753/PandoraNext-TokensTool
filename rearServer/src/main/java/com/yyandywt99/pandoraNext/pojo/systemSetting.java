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
     * tokensTool 拿到getTokenPassword
     * 为"getTokenPassword" 默认：123456
     * 默认拿getTokenPassword
     */
    private String getTokenPassword;

    /**
     * tokensTool 更新containerName(容器名)
     * 通过容器名实现开启，关闭，重新启动容器
     */
    private String containerName;


    /**
     * PandoraNext tls证书
     */
    private tls tls;

    /**
     * PandoraNext config.json位置
     */
    private String configPosition;

    /**
     * PandoraNext 接口地址添加前缀
     */
    private String isolated_conv_title;

    /**
     * PandoraNext 会话标题
     */
    private String proxy_api_prefix;

    /**
     * PandoraNext pool_token
     */
    private String pool_token;

}
