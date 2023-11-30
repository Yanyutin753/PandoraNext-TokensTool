package com.yyandywt99.pandoraNext.service;

import com.yyandywt99.pandoraNext.pojo.token;

import java.io.IOException;
import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-07 14:53
 */
public interface apiService {

    String addToken(token token) throws IOException;

    List<token> seleteToken(String name);

    String requiredToken(token tem);

    String deleteToken(String name);

    token autoUpdateSimpleToken(token token);

    String autoUpdateToken(String s);

    String getIp();

    String autoUpdateSessionToken(token token);

    String toUpdatePoolToken(String poolToken);
}
