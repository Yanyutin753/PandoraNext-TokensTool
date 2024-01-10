package com.tokensTool.pandoraNext.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.loginLog;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.service.impl.systemServiceImpl;
import com.tokensTool.pandoraNext.service.loginService;
import com.tokensTool.pandoraNext.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yangyang
 * @create 2023-11-17 14:15
 */
@Slf4j
@RestController()
@RequestMapping("/api")
public class loginColltroller {


    private static final String LOOPBACK_ADDRESS = "127.0.0.1";
    private static final String IPV6_ADDRESS = "0:0:0:0:0:0:0:1";
    private static final Integer MAX_REQUESTNUMBER = 15;
    private static HashMap<String, loginLog> ipList;

    static {
        ipList = new HashMap<>();
        log.info("初始化ipList成功！");
    }

    @Autowired
    private loginService loginService;
    @Autowired
    private systemServiceImpl systemService;

    public static void setIpList(HashMap<String, loginLog> ipList) {
        loginColltroller.ipList = ipList;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    private void clearIpList() {
        HashMap<String, loginLog> newIpList = new HashMap<>();
        setIpList(newIpList);
        log.info("重置ipList成功！");
    }


    public String getAddress(String ip) {
        if (ip.equals(LOOPBACK_ADDRESS) || ip.equals(IPV6_ADDRESS)) {
            return "本机";
        }
        OkHttpClient httpClient = new OkHttpClient();
        String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&amp;json=true";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            String result = response.body().string();
            int startIndex = result.indexOf("{");
            int endIndex = result.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1) {
                // Find the actual start and end of the JSON data
                int jsonStartIndex = result.indexOf("{", startIndex + 1);
                int jsonEndIndex = result.lastIndexOf("}", endIndex - 1);
                if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                    String json = result.substring(jsonStartIndex, jsonEndIndex + 1);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map resultMap = objectMapper.readValue(json, Map.class);
                    System.out.println("ip信息：" + resultMap);
                    String addr = (String) resultMap.get("addr");
                    return addr;
                } else {
                    log.info("No JSON object found in the response");
                }
            } else {
                log.info("No JSON object found in the response");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "未知";
    }

    /**
     * 登录用户接口
     * 如果错误30次，就会被封禁
     * 直到第二天凌晨0点才能正常使用
     *
     * @param setting
     * @param request
     * @return "jwt令牌！"or"NOT_LOGIN"
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody systemSetting setting, HttpServletRequest request) {
        String ip = getRequestIp(request);
        log.info("ip地址为" + ip + "的用户请求登陆接口");
        Integer outRequestNumber = 0;
        Integer inRequestNumber = 0;
        if (ipList.containsKey(ip)) {
            loginLog getLoginLog = ipList.get(ip);
            outRequestNumber = getLoginLog.getOutRequestNumber();
            inRequestNumber = getLoginLog.getInRequestNumber();
            if (outRequestNumber > MAX_REQUESTNUMBER) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String formatDateTime = now.format(formatter);
                loginLog loginLog = new loginLog(getAddress(ip), ip, outRequestNumber + 1, inRequestNumber, formatDateTime);
                ipList.put(ip, loginLog);
                return Result.error("ip为" + ip + "的用户你已被封禁！！！");
            }
        }
        boolean res = loginService.login(setting);
        if (res) {
            String s;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String formatDateTime = now.format(formatter);
                loginLog loginLog = new loginLog(getAddress(ip), ip, outRequestNumber, inRequestNumber + 1, formatDateTime);
                String password = JwtUtils.getJwtPassword();
                String username = setting.getLoginUsername();
                Map<String, Object> chaims = new HashMap();
                chaims.put("password", password);
                chaims.put("username", username);
                s = JwtUtils.generateJwt(chaims);
                ipList.put(ip, loginLog);
                return Result.success(s);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String formatDateTime = now.format(formatter);
            loginLog loginLog = new loginLog(getAddress(ip), ip, outRequestNumber + 1, inRequestNumber, formatDateTime);
            ipList.put(ip, loginLog);
            return Result.error("账号密码错误，请检查文件是否配置正确，还有" + (MAX_REQUESTNUMBER - outRequestNumber) + "次机会！");
        }
    }

    /**
     * 通过HttpServletRequest返回客户端真实IP地址（通过多级代理后也能获取到真实ip）
     */
    private String getRequestIp(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            //如果使用localhost访问，对于windows IPv6会返回0:0:0:0:0:0:0:1，将其转为127.0.0.1
            if (IPV6_ADDRESS.equals(ip)) {
                ip = LOOPBACK_ADDRESS;
            }
            return ip;
        } catch (Exception e) {
            log.error("getIpAddr error! e:", e);
        }
        return "";
    }

    /**
     * 验证是否登录成功
     *
     * @return 没登陆成功否则返回"NOT_LOGIN"
     */
    @PostMapping("/loginToken")
    public Result loginToken(@RequestParam("token") String token) {
        systemSetting systemSetting = systemService.selectSetting();
        String password = JwtUtils.getJwtPassword();
        String username = systemSetting.getLoginUsername();

        if (!StringUtils.hasLength(token)) {
            log.info("请求头token为空,返回未登录的信息");
            return Result.error("NOT_LOGIN");
        }
        try {
            Claims claims = JwtUtils.parseJWT(token);
            String resPassword = claims.get("password").toString();
            String resUsername = claims.get("username").toString();
            if (resPassword.equals(password) && resUsername.equals(username)) {
                log.info("令牌合法，可以正常登录");
                return Result.success("YES_LOGIN");
            }
            return Result.error("YES_LOGIN");
        } catch (Exception e) {
            log.info("解析令牌失败, 返回未登录错误信息");
            return Result.error("NOT_LOGIN");
        }
    }

    /**
     * 验证是否登录成功
     *
     * @return 没登陆成功否则返回"NOT_LOGIN"
     */
    @GetMapping("/selectLogin")
    public Result selectLogin() {
        try {
            List<loginLog> res = new ArrayList();
            ipList.forEach((key, value) -> res.add(value));
            return Result.success(res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    /**
//     * 通过https://www.taobao.com/help/getip.php
//     * 获取公网ip
//     *
//     * @return 公网ip
//     */
//    public String getIp() {
//        StringBuilder result = new StringBuilder();
//        BufferedReader in = null;
//        try {
//            URL realUrl = new URL("https://www.taobao.com/help/getip.php");
//            // 打开和URL之间的连接
//            URLConnection connection = realUrl.openConnection();
//            // 设置通用的请求属性
//            connection.setRequestProperty("accept", "*/*");
//            connection.setRequestProperty("connection", "Keep-Alive");
//            connection.setRequestProperty("user-agent",
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            // 建立实际的连接
//            connection.connect();
//            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 定义 BufferedReader输入流来读取URL的响应
//            in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result.append(line);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "失败";
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (Exception e2) {
//                e2.printStackTrace();
//                return "失败";
//            }
//        }
//        String str = result.toString().replace("ipCallback({ip:", "");
//        String ipStr = str.replace("})", "");
//        return ipStr.replace('"', ' ').trim();
//    }
}
