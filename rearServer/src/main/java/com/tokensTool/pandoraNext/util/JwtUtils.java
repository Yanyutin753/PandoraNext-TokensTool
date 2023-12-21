package com.tokensTool.pandoraNext.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * @author Yangyang
 * @create 2023-09-27 15:38
 */


@Slf4j
@Data
@NoArgsConstructor
public class JwtUtils {
    //硬编码的伤
    private static String signKey = "123456";
    // JWT里的内容
    private static String JwtPassword = "tokensTool";
    private static Long expire = 43200000L;

    public static String getSignKey() {
        return signKey;
    }

    /**
     * 更换signkey的值
     */
    public static void setSignKey(String key) {
        signKey = key;
    }

    public static String getJwtPassword() {
        return JwtPassword;
    }

    public static void setJwtPassword(String password) {
        JwtPassword = password;
    }

    /**
     * 生成JWT令牌
     *
     * @param claims JWT第二部分负载 payload 中存储的内容
     * @return
     */
    public static String generateJwt(Map<String, Object> claims) {
        String jwt = Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, getSignKey())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    /**
     * 解析JWT令牌
     *
     * @param jwt JWT令牌
     * @return JWT第二部分负载 payload 中存储的内容
     */
    public static Claims parseJWT(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSignKey())
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
