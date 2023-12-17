package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-12-10 11:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class poolToken {
    /**
     * pool_token 专属名（文件唯一）
     */
    private String poolName;

    /**
     * pool_token 值
     */
    private String poolToken;

    /**
     * pool_token 的分享token名数组
     */
    private List<String> shareTokens;

    /**
     * pool_token 注册时间
     */
    private String poolTime;

}
