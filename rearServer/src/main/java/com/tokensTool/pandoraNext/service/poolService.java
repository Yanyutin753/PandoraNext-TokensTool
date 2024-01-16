package com.tokensTool.pandoraNext.service;

import com.tokensTool.pandoraNext.pojo.poolToken;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-12-10 12:00
 */
public interface poolService {

    String addPoolToken(poolToken poolToken);

    String deletePoolToken(poolToken poolToken);

    String refreshSimplyToken(poolToken poolToken);

    String changePoolToken(poolToken poolToken);

    List<poolToken> selectPoolToken(String name);

    String refreshAllPoolTokens();

    String verifySimplyPoolToken(poolToken poolToken);

    String verifyAllPoolToken();

    String toRequirePoolToken(poolToken poolToken);
}
