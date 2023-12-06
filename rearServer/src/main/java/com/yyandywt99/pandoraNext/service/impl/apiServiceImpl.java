package com.yyandywt99.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yyandywt99.pandoraNext.pojo.systemSetting;
import com.yyandywt99.pandoraNext.pojo.token;
import com.yyandywt99.pandoraNext.service.apiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Yangyang
 * @create 2023-11-07 14:54
 */
@Slf4j
@Service
public class apiServiceImpl implements apiService {
    @Value("${deployPosition}")
    private String deployPosition;

    /**
     * 登录接口获取session_token或者access_token
     */
    private final String loginToken = "/api/auth/login";

    /**
     * 把session_token转化成access_token
     */
    private final String accessToken = "/api/auth/session";

    /**
     * 把access_token转化为share_token
     */
    private final String shareToken = "/api/token/register";

    /**
     * 把share_token转化成pool_token
     */
    private final String poolToken = "/api/pool/update";

    /**
     * 部署路径为默认的话，自动识别jar包路径下的文件
     */
    private String deploy = "default";



    /**
     * 通过判断是否需要自定义查询tokens.json文件位置
     * @return tokens.json文件位置
     * @throws IOException
     */
    public String selectFile() throws IOException {
        String projectRoot;
        if(deploy.equals(deployPosition)){
            projectRoot = System.getProperty("user.dir");
        }
        else{
            projectRoot = deployPosition;
        }
        String parent = projectRoot + File.separator + "tokens.json";
        File jsonFile = new File(parent);
        Path jsonFilePath = Paths.get(parent);
        // 如果 JSON 文件不存在或为空，创建一个新的 JSON 对象
        if (!jsonFile.exists() || jsonFile.length() == 0) {
            // 创建文件
            Files.createFile(jsonFilePath);
            System.out.println("tokens.json创建完成: " + jsonFilePath);
            // 往 tokens.json 文件中添加一个空数组，防止重启报错
            Files.writeString(jsonFilePath, "{}");
            System.out.println("空数组添加完成");
        }
        return parent;
    }

    /**
     * 打印token全部
     *
     * @return res（List<token> ）
     */
    @Override
    public List<token> selectToken(String name) {
        List<token> res = new ArrayList<>();
        try {
            String parent = selectFile();
            log.info(parent);
            File jsonFile = new File(parent);
            ObjectMapper objectMapper = new ObjectMapper();
            // 如果 JSON 文件不存在或为空，则创建一个新的 JSON 对象并写入空数组
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                Files.writeString(Paths.get(parent), "{}");
                log.info("新建tokens.json，并初始化tokens.json成功！");
                return res;
            }
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));
            // 遍历所有字段
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String nodeName = entry.getKey();
                if (nodeName.contains(name)) {
                    token temRes = new token();
                    temRes.setName(nodeName);
                    // 获取对应的节点
                    JsonNode temNode = rootNode.get(nodeName);
                    temRes.setUsername(temNode.has("username") ? temNode.get("username").asText() : "");
                    temRes.setToken(temNode.has("token") ? temNode.get("token").asText() : "");
                    temRes.setAccess_token(temNode.has("access_token") ? temNode.get("access_token").asText() : "第一次加载数据请先点击生成");
                    temRes.setShare_token(temNode.has("share_token") ? temNode.get("share_token").asText() : "第一次加载数据请先点击生成");
                    temRes.setUserPassword(temNode.has("userPassword") ? temNode.get("userPassword").asText() : "");
                    temRes.setShared(temNode.has("shared") ? temNode.get("shared").asBoolean() : false);
                    temRes.setShow_user_info(temNode.has("show_user_info") ? temNode.get("show_user_info").asBoolean() : false);
                    temRes.setPlus(temNode.has("plus") ? temNode.get("plus").asBoolean() : false);
                    temRes.setSetPoolToken(temNode.has("setPoolToken") ? temNode.get("setPoolToken").asBoolean() : false);
                    temRes.setPassword(temNode.has("password") ? temNode.get("password").asText() : "");
                    temRes.setUpdateTime(temNode.has("updateTime") ? temNode.get("updateTime").asText() : "");
                    res.add(temRes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }

    /**
     * 添加token
     * 并添加对应keys
     *
     * @return "添加成功！"or"添加失败,检查你的token是否正确或登录是否过期！"
     */
    @Override
    public String addToken(token token) {
        String res = "";
        //不填token,填账号密码
        if (token.getToken() == null || token.getToken().length() == 0) {
            res = updateSessionToken(token);
            if (res != null) {
                token.setToken(res);
            } else {
                return "添加失败,检查你的账号密码是否正确或刷新token网址有误！";
            }
        }
        try {
            String parent = selectFile();
            File jsonFile = new File(parent);
            Path jsonFilePath = Paths.get(parent);
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode;
            // 如果 JSON 文件不存在，创建一个新的 JSON 对象
            if (!jsonFile.exists()) {
                // 创建文件
                Files.createFile(jsonFilePath);
                System.out.println("tokens.json创建完成: " + jsonFilePath);
                rootNode = objectMapper.createObjectNode();
            } else {
                if (Files.exists(jsonFilePath) && Files.size(jsonFilePath) > 0) {
                    rootNode = objectMapper.readTree(jsonFile).deepCopy();
                } else {
                    rootNode = objectMapper.createObjectNode();
                }
            }
            // 创建要添加的新数据
            ObjectNode newData = objectMapper.createObjectNode();

            newData.put("token", token.getToken());

            String access_token = getAccessToken(token);
            if(access_token != null){
                newData.put("access_token",access_token);
                token.setAccess_token(access_token);
                String share_token = getShareToken(token);
                if(share_token != null && share_token.length() > 0){
                    token.setShare_token(share_token);
                    newData.put("share_token",share_token);
                }
            }
//            else{
//                return "出错了，没有拿到access_token和share_token";
//            }
            newData.put("username", token.getUsername());
            newData.put("userPassword", token.getUserPassword());
            newData.put("shared", token.isShared());
            newData.put("show_user_info", token.isShow_user_info());
            newData.put("plus", token.isPlus());
            newData.put("setPoolToken",token.isSetPoolToken());


            // 检查是否需要 TokenPassword
            if (token.getPassword() != null && token.getPassword().length() > 0) {
                newData.put("password", token.getPassword());
            } else {
                newData.put("password", "");
            }

            LocalDateTime now = LocalDateTime.now();
            newData.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 将新数据添加到 JSON 树中
            rootNode.put(token.getName(), newData);
            // 将修改后的数据写回到文件
            objectMapper.writeValue(jsonFile, rootNode);
            log.info("数据成功添加到 JSON 文件中。");
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return "添加失败！";
        }
    }


    /**
     * 修改token值或者其他
     * 会通过删除相应的keys,并添加新keys(会检验是否Token合格)
     *
     * @return "修改成功！"or"修改失败"or修改失败,检查你的token是否正确！
     */
    @Override
    public String requiredToken(token tem) {
        try {
            String parent = selectFile();
            log.info(parent);
            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));

            // 要修改的节点名称
            String nodeNameToModify = tem.getName();

            // 获取要修改的节点
            JsonNode nodeToModify = rootNode.get(nodeNameToModify);

            if (nodeToModify != null && nodeToModify.isObject()) {
                // 创建新的 ObjectNode，并复制原始节点内容
                ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                newObjectNode.setAll((ObjectNode) rootNode);

                // 获取要修改的节点
                ObjectNode nodeToModifyInNew = newObjectNode.with(nodeNameToModify);

                // 获取之前的节点值
                String previousToken = nodeToModifyInNew.has("token") ? nodeToModifyInNew.get("token").asText() : null;
                // 修改节点的值
                nodeToModifyInNew.put("token", tem.getToken());
                nodeToModifyInNew.put("username", tem.getUsername());
                nodeToModifyInNew.put("userPassword", tem.getUserPassword());
                nodeToModifyInNew.put("shared", tem.isShared());
                nodeToModifyInNew.put("show_user_info", tem.isShow_user_info());
                nodeToModifyInNew.put("plus", tem.isPlus());
                nodeToModifyInNew.put("setPoolToken", tem.isSetPoolToken());

                // 检查是否需要 TokenPassword
                if (tem.getPassword() != null && tem.getPassword().length() > 0) {
                    nodeToModifyInNew.put("password", tem.getPassword());
                } else {
                    nodeToModifyInNew.put("password", "");
                }
                if (tem.getAccess_token() != null && tem.getAccess_token().length() > 0) {
                    nodeToModifyInNew.put("access_token", tem.getAccess_token());
                }
                log.info("tem里的share_token"+tem.getShare_token());
                if (tem.getShare_token() != null && tem.getShare_token().length() > 0) {
                    nodeToModifyInNew.put("share_token", tem.getShare_token());
                    log.info("添加share_token成功！");
                }
                if(! previousToken.equals(tem.getToken())){
                    //获取access_token或share_token
                    String access_token = getAccessToken(tem);
                    if(access_token != null){
                        tem.setAccess_token(access_token);
                        String share_token = getShareToken(tem);
                        nodeToModifyInNew.put("access_token",access_token);
                        if(share_token != null){
                            nodeToModifyInNew.put("share_token",share_token);
                        }
                        else{
                            nodeToModifyInNew.put("share_token","");
                        }
                    }
                    else{
                        nodeToModifyInNew.put("access_token","");
                    }
                    LocalDateTime now = LocalDateTime.now();
                    nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                log.info("修改成功");
                return "修改成功！";
            } else {
                System.out.println("节点未找到或不是对象,请检查tokens.json！ " + nodeNameToModify);
                return "节点未找到或不是对象！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "修改失败！";
    }


    /**
     * 删除token
     * 并删除对应keys
     *
     * @return "删除成功！"or"删除失败"
     */
    @Override
    public String deleteToken(String name) {
        try {
            String parent = selectFile();
            log.info(parent);

            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));

            // 检查要删除的节点是否存在
            JsonNode nodeToRemove = rootNode.get(name);
            if (nodeToRemove != null) {
                // 创建新的 ObjectNode，并复制原始节点内容
                ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                newObjectNode.setAll((ObjectNode) rootNode);

                // 删除节点
                newObjectNode.remove(name);

                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);

                log.info("删除成功");
                return "删除成功！";
            } else {
                System.out.println("Node not found: " + name);
                return "节点未找到！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "删除失败";
    }


    @Autowired
    private systemServiceImpl systemService;
    /**
     * 自动更新Token方法
     * 通过api/auth/login拿到session_token
     * 更换tokens.json里存储的Token
     * 账号为token.getUserName()
     * 密码为token.getUserPassword()
     */
    public String updateSessionToken(token token) {
        String url;
        systemSetting systemSetting = systemService.selectSetting();
        if(systemSetting.getAutoToken_url().equals("default")){
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://" + getIp() + ":" + parts[1] +"/" + systemSetting.getProxy_api_prefix() +loginToken;
        }
        else{
            url = systemSetting.getAutoToken_url() + loginToken;
        }
        log.info("将通过这个网址请求登录信息："+url);
        try {
            // 创建HttpClient实例
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost(url);

            // 使用MultipartEntityBuilder构建表单数据
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("username", token.getUsername(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", token.getUserPassword(), ContentType.TEXT_PLAIN);

            // 设置请求实体
            httpPost.setEntity(builder.build());

            //设置用户代理
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
            httpPost.setHeader("User-Agent", userAgent);

            // 执行HTTP请求
            HttpResponse response = httpClient.execute(httpPost);
            log.info(response.toString());
            int statusCode = response.getStatusLine().getStatusCode();
            // 获得响应数据
            String responseContent = EntityUtils.toString(response.getEntity());
            // 处理响应数据
            String resToken = null;
            try {
                JSONObject jsonResponse = new JSONObject(responseContent);
                // 提取返回的数据
                log.info(jsonResponse.toString());
                resToken = jsonResponse.getString("session_token");
                httpClient.close();
            } catch (JSONException e) {
                e.printStackTrace();
                httpClient.close();
            }
            //关闭进程
            if (statusCode == 200 && resToken.length() > 400) {
                //用来防止请求的token出现问题，回退token值
                log.info("Request was successful");
                return resToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccessToken(token token) {
        String url;
        systemSetting systemSetting = systemService.selectSetting();
        if(systemSetting.getAutoToken_url().equals("default")){
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://" + getIp() + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + accessToken;
        }
        else{
            url = systemSetting.getAutoToken_url() + accessToken;
        }
        log.info("将通过这个网址请求登录信息："+url);
        try {
            // 创建HttpClient实例
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost(url);

            // 使用MultipartEntityBuilder构建表单数据
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("session_token", token.getToken(), ContentType.TEXT_PLAIN);

            // 设置请求实体
            httpPost.setEntity(builder.build());

            // 执行HTTP请求
            HttpResponse response = httpClient.execute(httpPost);
            log.info(response.toString());
            int statusCode = response.getStatusLine().getStatusCode();
            // 获得响应数据
            String responseContent = EntityUtils.toString(response.getEntity());
            // 处理响应数据
            String resToken = null;
            try {
                JSONObject jsonResponse = new JSONObject(responseContent);
                // 提取返回的数据
                log.info(jsonResponse.toString());
                resToken = jsonResponse.getString("access_token");
                log.info("access_token更新为："+resToken);
                httpClient.close();
            } catch (JSONException e) {
                e.printStackTrace();
                httpClient.close();
            }
            //关闭进程
            if (statusCode == 200 && resToken.length() > 400) {
                //用来防止请求的token出现问题，回退token值
                log.info("请求成功，Url的路径为："+url);
                return resToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getShareToken(token token) {
        String url;
        systemSetting systemSetting = systemService.selectSetting();
        if (systemSetting.getAutoToken_url().equals("default")) {
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://" + getIp() + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + shareToken;
        } else {
            url = systemSetting.getAutoToken_url() + shareToken;
        }
        // 假设expires_in为0
        int expires_in = 0;
        boolean show_conversations = true;
        log.info("将通过这个网址请求登录信息：" + url);
        String data = "unique_name=" + token.getName() + "&access_token=" + token.getAccess_token() + "&expires_in=" + expires_in + "&show_conversations=" + show_conversations;
        String tokenKey = "";
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // 设置请求方法为POST
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // 发送POST数据
            OutputStream os = con.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            // 获取响应
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String responseJson = response.toString();
                tokenKey = new JSONObject(responseJson).getString("token_key");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String errStr = response.toString().replace("\n", "").replace("\r", "").trim();
                System.out.println("share token failed: " + errStr);
                return null;
            }

            // 使用正则表达式匹配字符串
            String shareToken = tokenKey;
            if (shareToken.matches("^(fk-|pk-).*")) {
                log.info("open_ai_api_key 被更新为: " + shareToken);
                return shareToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPoolToken(String pool_token) {
        String url;
        systemSetting systemSetting = systemService.selectSetting();
        if(systemSetting.getAutoToken_url().equals("default")){
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://" + getIp() + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + poolToken;
        }
        else{
            url = systemSetting.getAutoToken_url() + poolToken;
        }
        log.info("将通过这个网址请求登录信息："+url);
        try {
            // 创建HttpClient实例
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost(url);

            // 使用MultipartEntityBuilder构建表单数据
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            List<token> tokens = selectToken("");
            StringBuffer resToken = new StringBuffer();
            for(token token : tokens){
                if(token.getShare_token() != null && token.isSetPoolToken() ){
                    resToken.append(token.getShare_token()+"\n");
                }
            }
            log.info("更新之前pool_token为："+pool_token);
            builder.addTextBody("share_tokens", resToken.toString(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pool_token",pool_token, ContentType.TEXT_PLAIN);
            // 设置请求实体
            httpPost.setEntity(builder.build());
            // 执行HTTP请求
            HttpResponse response = httpClient.execute(httpPost);
            log.info(response.toString());
            int statusCode = response.getStatusLine().getStatusCode();
            // 获得响应数据
            String responseContent = EntityUtils.toString(response.getEntity());
            // 处理响应数据
            String resPoolToken = null;
            try {
                JSONObject jsonResponse = new JSONObject(responseContent);
                // 提取返回的数据
                log.info(jsonResponse.toString());
                resPoolToken = jsonResponse.getString("pool_token");
                log.info("pool_token更新为："+resPoolToken);
                log.info("一共运行了"+jsonResponse.getString("count")+"条share_token");
                httpClient.close();
            } catch (JSONException e) {
                e.printStackTrace();
                httpClient.close();
            }
            //关闭进程
            if (statusCode == 200 && resPoolToken.contains("pk")) {
                //用来防止请求的token出现问题，回退token值
                log.info("请求pool_token成功，Url的路径为："+url);
                return resPoolToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 刷新Token
     *
     * @return "更新成功" or "更新失败"
     */
    @Override
    public token autoUpdateSimpleToken(token token) {
        if (token == null) {
            log.info("未查询到相关数据");
            return null;
        }
        String access_token = getAccessToken(token);
        if (access_token != null) {
            token.setAccess_token(access_token);
            //执行获取share_token操作
            String share_token = getShareToken(token);
            token.setShare_token(share_token);
            log.info("更新"+share_token);
            if (share_token != null) {
                String res = requiredToken(token);
                log.info(res);
                if(res.contains("成功")){
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * 自动更新Token
     * 更换tokensTool里存储的Token的access_token和share_token
     * @return "更新成功" or "更新失败"
     */
    public String autoUpdateToken(String name) {
        List<token> resTokens = selectToken(name);
        int newToken = 0;
        for (token token : resTokens) {
            token res = autoUpdateSimpleToken(token);
            if(res != null){
                newToken++;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (newToken == 0) {
            return "自动修改Token失败！";
        } else {
            return "自动修改Token成功：" + newToken + "失败：" + (resTokens.size() - newToken);
        }
    }

    public String getIp(){
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            URL realUrl = new URL("https://www.taobao.com/help/getip.php");
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "失败";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return "失败";
            }
        }
        String str = result.toString().replace("ipCallback({ip:", "");
        String ipStr = str.replace("})", "");
        return ipStr.replace('"', ' ').trim();
    }

    @Override
    public String autoUpdateSessionToken(token token) {
        if (token == null) {
            log.info("未查询到相关数据");
            return null;
        }
        try {
            String sessionToken = updateSessionToken(token);
            if(sessionToken != null){
                return sessionToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toUpdatePoolToken(String poolToken) {
        String res;
        try {
            res = getPoolToken(poolToken);
            if(poolToken != null){
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}