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
//    {"current":4,
//            "ip":"121.37.243.173",
//            "license_id":"etYUwfjFn89LNRtbcdFMPlTcdP9hLSJS6wHZEpEQTR0",
//            "total":"2000","ttl":44679}
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
