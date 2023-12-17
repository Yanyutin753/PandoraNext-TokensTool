package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yangyang
 * @create 2023-12-16 20:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PandoraLimit {
    /**
     * 用量
     */
    private Integer current;
    /**
     * 绑定ip
     */
    private String ip;
    /**
     * 总余额
     */
    private Integer total;
    /**
     * 重启时间
     */
    private Integer ttl;
}
