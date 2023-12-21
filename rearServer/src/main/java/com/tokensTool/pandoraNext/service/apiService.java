package com.tokensTool.pandoraNext.service;

import com.tokensTool.pandoraNext.pojo.PandoraLimit;
import com.tokensTool.pandoraNext.pojo.token;

import java.io.IOException;
import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-07 14:53
 */
public interface apiService {
    /**
     * 添加token
     * 并添加对应keys
     *
     * @return "添加成功！"or"添加失败,检查你的token是否正确或登录是否过期！"
     */
    String addToken(token token) throws IOException;

    /**
     * 打印token全部
     *
     * @return res（List<token> ）
     */
    List<token> selectToken(String name);

    /**
     * 修改token值或者其他
     * 会通过删除相应的keys,并添加新keys(会检验是否Token合格)
     *
     * @return "修改成功！"or"修改失败"or修改失败,检查你的token是否正确！
     */
    String requiredToken(token tem);

    /**
     * 删除token
     * 并删除对应keys
     *
     * @return "删除成功！"or"删除失败"
     */
    String deleteToken(String name);

    /**
     * 更换tokensTool里存储的Token的access_token和share_token
     *
     * @return "更新成功" or "更新失败"
     */
    token autoUpdateSimpleToken(token token);


    /**
     * 自动更新Token
     * 更换tokensTool里存储的Token的access_token和share_token
     *
     * @return "更新成功" or "更新失败"
     */
    String autoUpdateToken(String s);


    /**
     * 通过https://www.taobao.com/help/getip.php
     * 获取公网ip
     *
     * @return 公网ip
     */
    String getIp();

    /**
     * 自动更新session_token时间80天
     *
     * @return
     */
    String autoUpdateSessionToken(token token);


    /**
     * 注销pool_token
     *
     * @param poolToken
     * @return
     */
    String deletePoolToken(String poolToken);

    /**
     * 获取pandoraNext的余额
     *
     * @return
     */
    PandoraLimit getPandoraLimit();

    /**
     * 刷新session，更新access_token和share_token
     *
     * @param token
     * @return
     */
    token updateSession(token token);
}
