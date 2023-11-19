package com.yyandywt99.pandoraNext.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override //目标资源方法运行前运行, 返回true: 放行, 放回false, 不放行
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        //1.获取请求url。
        String url = req.getRequestURL().toString();
        log.info("请求的url: {}",url);

        //2.判断请求url中是否包含login，如果包含，说明是登录操作，放行。
        if(url.contains("login")){
            log.info("登录操作, 放行...");
            return true;
        }

        //3.获取请求头中的令牌（token）。
        String jwt = req.getHeader("Authorization");
        log.info(jwt);

        //4.判断令牌是否存在，如果不存在，返回错误结果（未登录）。
        if(!StringUtils.hasLength(jwt) || !jwt.startsWith("Bearer ")){
            log.info("请求头token空或没有Bearer ,返回未登录的信息");
            Result error = Result.error("NOT_LOGIN");
            //手动转换 对象--json --------> 阿里巴巴fastJSON
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;
        }
        jwt = jwt.substring(7);
        //5.解析token，如果解析失败，返回错误结果（未登录）。
        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {//jwt解析失败
            e.printStackTrace();
            log.info("解析令牌失败, 返回未登录错误信息");
            Result error = Result.error("NOT_LOGIN");
            //手动转换 对象--json --------> 阿里巴巴fastJSON
            String notLogin = JSONObject.toJSONString(error);
            resp.getWriter().write(notLogin);
            return false;
        }

        //6.放行。
        log.info("令牌合法, 放行");
        return true;
    }

    @Override //目标资源方法运行后运行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            System.out.println("postHandle ...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override //视图渲染完毕后运行, 最后运行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            System.out.println("afterCompletion...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
