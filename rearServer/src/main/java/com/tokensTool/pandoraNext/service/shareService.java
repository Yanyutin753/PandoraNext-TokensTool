package com.tokensTool.pandoraNext.service;

import com.tokensTool.pandoraNext.pojo.shareToken;

import java.util.List;

/**
 * @author Yangyang
 * @create 2024-01-08 10:22
 */
public interface shareService {
    List<shareToken> selectShareToken(String name);

    String deleteShareToken(shareToken shareToken);


    String requireShareToken(shareToken shareToken);

    String addShareToken(shareToken shareToken);
}
