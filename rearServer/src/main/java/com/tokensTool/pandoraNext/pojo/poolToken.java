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
    private String poolName;

    private String poolToken;

    private List<String> shareTokens;

    private String poolTime;

}
