package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yangyang
 * @create 2023-12-29 0:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class loginLog {
    private String ipAddress;
    private String requestIp;
    private Integer outRequestNumber;
    private Integer inRequestNumber;
    private String lastLoginTime;
}
