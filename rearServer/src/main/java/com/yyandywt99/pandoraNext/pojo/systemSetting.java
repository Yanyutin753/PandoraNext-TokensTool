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
     * 白名单（null则不限制，为空数组[]则限制所有账号）
     */
    private String whitelist;
}
