package com.yyandywt99.pandoraNext.aop;

import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yangyang
 * @create 2023-10-07 13:35
 */
@Slf4j
@Component
//切面类
@Aspect
public class LogAspect {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Around("@annotation(com.yyandywt99.pandoraNext.anno.Log)")

    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String jwt = httpServletRequest.getHeader("Authorization");
        Integer operateUser = null;
        if(jwt != null){
            try {
                jwt = jwt.substring(7);
                Claims claims = JwtUtils.parseJWT(jwt);
            } catch (Exception e) {
                log.info(e.toString());
            }
        }
        return Result.error("Not_LOGIN");

    }
}
