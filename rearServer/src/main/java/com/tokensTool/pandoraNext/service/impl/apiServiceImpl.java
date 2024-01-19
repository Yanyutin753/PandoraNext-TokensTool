package com.tokensTool.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tokensTool.pandoraNext.pojo.PandoraLimit;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.apiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Yangyang
 * @create 2023-11-07 14:54
 */
@Slf4j
@Service
public class apiServiceImpl implements apiService {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build();
    /**
     * 登录接口获取session_token或者access_token
     */
    private final String sessionToken = "/api/auth/login";
    /**
     * 登录接口获取refresh_token
     */
    private final String refreshToken = "/api/auth/login2";

    /**
     * 把refresh_token转化成access_token
     */
    private final String reAccessToken = "/api/auth/refresh";

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
    private final String deploy = "default";
    @Value("${deployPosition}")
    private String deployPosition;
    @Autowired
    private systemServiceImpl systemService;

    public String initializeTokenJson() {
        try {
            String parent = selectFile();
            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));
            // 遍历根节点的所有子节点
            if (rootNode.isObject()) {
                ObjectNode rootObjectNode = (ObjectNode) rootNode;
                // 遍历所有子节点
                Iterator<Map.Entry<String, JsonNode>> fields = rootObjectNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    // 获取子节点的名称
                    String nodeName = entry.getKey();
                    // 获取子节点
                    JsonNode nodeToModify = entry.getValue();
                    if (nodeToModify != null && nodeToModify.isObject()) {
                        // 创建新的 ObjectNode，并复制原始节点内容
                        ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                        newObjectNode.setAll(rootObjectNode);
                        // 获取要修改的节点
                        ObjectNode nodeToModifyInNew = newObjectNode.with(nodeName);
                        // 初始化checkSession的值为true
                        if (!nodeToModifyInNew.has("useRefreshToken")) {
                            nodeToModifyInNew.put("useRefreshToken", false);
                            log.info("为节点 " + nodeName + " 添加 useRefreshToken 变量成功！");
                        }
                        if (nodeToModifyInNew.has("shared") && !nodeToModifyInNew.has("auto_shared")) {
                            nodeToModifyInNew.put("auto_shared", nodeToModifyInNew.get("shared").asBoolean());
                            log.info("为节点 " + nodeName + " 添加 auto_shared 变量成功！");
                        }
                        // 将修改后的 newObjectNode 写回文件
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                    }
                }
                return "为所有子节点添加 useRefreshToken 变量成功！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "为所有子节点添加 useRefreshToken 变量失败！";
    }

    /**
     * 通过判断是否需要自定义查询tokens.json文件位置
     *
     * @return tokens.json文件位置
     * @throws IOException
     */
    public String selectFile() throws IOException {
        String projectRoot;
        if (deploy.equals(deployPosition)) {
            projectRoot = System.getProperty("user.dir");
        } else {
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
            log.info("请求路径查询token：" + parent);
            File jsonFile = new File(parent);
            ObjectMapper objectMapper = new ObjectMapper();
            // 如果 JSON 文件不存在或为空，则创建一个新的 JSON 对象并写入空数组
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                Files.writeString(Paths.get(parent), "{}");
                log.error("未找到tokens.json,新建tokens.json并初始化tokens.json成功！");
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
                    temRes.setAccess_token(temNode.has("access_token") ? temNode.get("access_token").asText() : "未开启pool_token无法生成");
                    temRes.setShare_token(temNode.has("share_token") ? temNode.get("share_token").asText() : "未开启pool_token无法生成");
                    temRes.setUserPassword(temNode.has("userPassword") ? temNode.get("userPassword").asText() : "");
                    temRes.setShared(temNode.has("shared") && temNode.get("shared").asBoolean());
                    temRes.setAuto_shared(temNode.has("auto_shared") && temNode.get("auto_shared").asBoolean());
                    temRes.setShow_user_info(temNode.has("show_user_info") && temNode.get("show_user_info").asBoolean());
                    temRes.setPlus(temNode.has("plus") && temNode.get("plus").asBoolean());
                    temRes.setSetPoolToken(temNode.has("setPoolToken") && temNode.get("setPoolToken").asBoolean());
                    temRes.setPassword(temNode.has("password") ? temNode.get("password").asText() : "");
                    temRes.setUpdateTime(temNode.has("updateTime") ? temNode.get("updateTime").asText() : "");
                    temRes.setUseRefreshToken(temNode.has("useRefreshToken") && temNode.get("useRefreshToken").asBoolean());
                    //是否session有效
                    temRes.setCheckSession(!temNode.has("checkSession") || temNode.get("checkSession").asBoolean());
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
     * 添加checkSession变量,初始化为true
     * 启动的时候自动全部添加
     */
    public String initializeCheckSession() {
        try {
            String parent = selectFile();
            ObjectMapper objectMapper = new ObjectMapper();

            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));

            // 遍历根节点的所有子节点
            if (rootNode.isObject()) {
                ObjectNode rootObjectNode = (ObjectNode) rootNode;

                // 遍历所有子节点
                Iterator<Map.Entry<String, JsonNode>> fields = rootObjectNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();

                    // 获取子节点的名称
                    String nodeName = entry.getKey();

                    // 获取子节点
                    JsonNode nodeToModify = entry.getValue();

                    if (nodeToModify != null && nodeToModify.isObject()) {
                        // 创建新的 ObjectNode，并复制原始节点内容
                        ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                        newObjectNode.setAll(rootObjectNode);

                        // 获取要修改的节点
                        ObjectNode nodeToModifyInNew = newObjectNode.with(nodeName);

                        // 初始化checkSession的值为true
                        if (!nodeToModifyInNew.has("checkSession")) {
                            nodeToModifyInNew.put("checkSession", true);
                            log.info("为节点 " + nodeName + " 添加 checkSession 变量成功！");
                        }

                        // 将修改后的 newObjectNode 写回文件
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                    }
                }
                return "为所有子节点添加 checkSession 变量成功！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "为所有子节点添加 checkSession 变量失败！";
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
            if (token.isUseRefreshToken()) {
                res = updateRefreshToken(token);
            } else {
                res = updateSessionToken(token);
            }
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
                log.info("tokens.json创建完成: " + jsonFilePath);
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

            String access_token = null;
            if (token.isSetPoolToken()) {
                access_token = getAccessToken(token);
                if (access_token != null) {
                    newData.put("access_token", access_token);
                    token.setAccess_token(access_token);
                    String share_token = getShareToken(token);
                    if (share_token != null && share_token.length() > 0) {
                        token.setShare_token(share_token);
                        newData.put("share_token", share_token);
                    }
                } else {
                    if (token.isUseRefreshToken()) {
                        return "添加失败！请确保自己填写的token为正确的refresh_token";
                    } else {
                        return "添加失败！请确保自己填写的token为正确的session_token";
                    }
                }
            }

            newData.put("username", token.getUsername());
            newData.put("userPassword", token.getUserPassword());
            newData.put("shared", token.isShared());
            newData.put("auto_shared", token.isAuto_shared());
            newData.put("show_user_info", token.isShow_user_info());
            newData.put("plus", token.isPlus());
            newData.put("setPoolToken", token.isSetPoolToken());
            //是否使用refresh_token来进行
            newData.put("useRefreshToken", token.isUseRefreshToken());
            newData.put("checkSession", true);

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
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
            return "添加成功！";
        } catch (IOException e) {
            e.printStackTrace();
            return "添加失败！";
        }
    }

    /**
     * 修改token值或者其他
     *
     * @return "修改成功！"or"修改失败"or修改失败,检查你的token是否正确！
     */
    @Override
    public String requiredToken(token tem) {
        try {
            String parent = selectFile();
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
                boolean previousSetPoolToken = nodeToModifyInNew.has("setPoolToken") && nodeToModifyInNew.get("setPoolToken").asBoolean();
                // 获取之前的节点值
                boolean previousUseRefreshToken = nodeToModifyInNew.has("useRefreshToken") && nodeToModifyInNew.get("useRefreshToken").asBoolean();
                // 初始修改相应的值
                require_beginToken(tem, nodeToModifyInNew, true);
                //web条件 api转web web转api 消耗余额
                if (previousSetPoolToken != tem.isSetPoolToken()) {
                    if (!tem.isSetPoolToken()) {
                        nodeToModifyInNew.put("token", tem.getUsername() + "," + tem.getUserPassword());
                        nodeToModifyInNew.put("share_token", "未开启API开关无法生成");
                        nodeToModifyInNew.put("access_token", "未开启API开关无法生成");
                        LocalDateTime now = LocalDateTime.now();
                        nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        // 将修改后的 newObjectNode 写回文件
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                        return "修改成功！";
                    }
                    // token相同的情况下 web 转 session_token
                    else if (!tem.isUseRefreshToken() && previousToken.equals(tem.getToken())) {
                        String reSessionToken = updateSessionToken(tem);
                        if (reSessionToken != null) {
                            reupdate(reSessionToken, tem, nodeToModifyInNew);
                        } else {
                            return "修改失败，请确保你的账号密码是否正确且proxy的url配置是否正确，或者余额不足";
                        }
                    }
                    // token相同的情况下 web 转 refresh_token
                    else if (tem.isUseRefreshToken() && previousToken.equals(tem.getToken())) {
                        String refreshToken = updateRefreshToken(tem);
                        if (refreshToken != null) {
                            reupdate(refreshToken, tem, nodeToModifyInNew);
                        } else {
                            return "修改失败，请确保你的账号密码是否正确且proxy的url配置是否正确，或者余额不足";
                        }
                    }
                }

                // token不相同，直接看token是否对应
                // web转api 填写相应的token
                // session和refresh之间的互转 填写相应的token

                if (!previousToken.equals(tem.getToken())) {
                    String access_token = getAccessToken(tem);
                    if (access_token != null) {
                        tem.setAccess_token(access_token);
                        String share_token = getShareToken(tem);
                        nodeToModifyInNew.put("access_token", access_token);
                        if (share_token != null) {
                            nodeToModifyInNew.put("checkSession", true);
                            nodeToModifyInNew.put("share_token", share_token);
                        } else {
                            return "access_token转share_token失败！";
                        }
                        // 将修改后的 newObjectNode 写回文件
                        LocalDateTime now = LocalDateTime.now();
                        nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                        return "修改成功！";
                    } else {
                        return tem.isUseRefreshToken() ? "添加失败！请填写正确的refresh_token或关闭使用refresh_token后重新尝试" : "修改失败！请填写正确的session_token或开启使用refresh_token后重新尝试";
                    }
                }

                // 在不改变token的值的API模式下，session和refresh之间的互相转换
                if (previousUseRefreshToken != tem.isUseRefreshToken()) {
                    if (tem.isUseRefreshToken()) {
                        String refreshToken = updateRefreshToken(tem);
                        if (refreshToken != null) {
                            reupdate(refreshToken, tem, nodeToModifyInNew);
                        } else {
                            return "修改失败，请确保你的账号密码是否正确且proxy的url配置是否正确，或者余额不足";
                        }
                    } else {
                        String reSessionToken = updateSessionToken(tem);
                        if (reSessionToken != null) {
                            reupdate(reSessionToken, tem, nodeToModifyInNew);
                        } else {
                            return "修改失败，请确保你的账号密码是否正确且proxy的url配置是否正确，或者余额不足";
                        }
                    }
                }
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                return "修改成功！";
            } else {
                log.error("节点未找到或不是对象,请检查tokens.json！ " + nodeNameToModify);
                return "节点未找到或不是对象！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "修改失败！";
    }

    public void reupdate(String token, token tem, ObjectNode nodeToModifyInNew) {
        tem.setToken(token);
        nodeToModifyInNew.put("token", token);
        require_UpdateToken(tem, nodeToModifyInNew);
        LocalDateTime now = LocalDateTime.now();
        nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }


    public void require_beginToken(token tem, ObjectNode nodeToModifyInNew, boolean autoShare) {
        // 修改节点的值
        if (autoShare) {
            nodeToModifyInNew.put("auto_shared", tem.isAuto_shared());
        }
        nodeToModifyInNew.put("token", tem.getToken());
        nodeToModifyInNew.put("username", tem.getUsername());
        nodeToModifyInNew.put("userPassword", tem.getUserPassword());
        nodeToModifyInNew.put("shared", tem.isShared());
        nodeToModifyInNew.put("show_user_info", tem.isShow_user_info());
        nodeToModifyInNew.put("plus", tem.isPlus());
        nodeToModifyInNew.put("setPoolToken", tem.isSetPoolToken());
        nodeToModifyInNew.put("access_token", tem.getAccess_token());
        nodeToModifyInNew.put("share_token", tem.getShare_token());
        nodeToModifyInNew.put("checkSession", tem.isCheckSession());
        nodeToModifyInNew.put("useRefreshToken", tem.isUseRefreshToken());
        if (tem.getPassword() != null && tem.getPassword().length() > 0) {
            nodeToModifyInNew.put("password", tem.getPassword());
        } else {
            nodeToModifyInNew.put("password", "");
        }
    }

    /**
     * 专门给生成函数写的修改token的值
     *
     * @param tem
     * @return
     */
    public String product_requireToken(token tem, boolean autoShare) {
        try {
            String parent = selectFile();
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
                // 初始修改相应的值
                require_beginToken(tem, nodeToModifyInNew, autoShare);
                if (!previousToken.equals(tem.getToken())
                        && tem.isSetPoolToken()) {
                    // 将修改后的 newObjectNode 写回文件
                    LocalDateTime now = LocalDateTime.now();
                    nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                    return "修改成功！";
                }
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                return "修改成功！";
            } else {
                log.error("节点未找到或不是对象,请检查tokens.json！ " + nodeNameToModify);
                return "节点未找到或不是对象！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "修改失败！";
    }


    public void require_UpdateToken(token tem, ObjectNode nodeToModifyInNew) {
        try {
            String access_token = getAccessToken(tem);
            if (access_token != null) {
                tem.setAccess_token(access_token);
                String share_token = getShareToken(tem);
                nodeToModifyInNew.put("access_token", access_token);
                nodeToModifyInNew.put("checkSession", true);
                if (share_token != null) {
                    nodeToModifyInNew.put("share_token", share_token);
                } else {
                    nodeToModifyInNew.put("share_token", "检查session或refresh是否过期，请重新刷新获取");
                }
            } else {
                nodeToModifyInNew.put("access_token", "检查session或refresh是否过期，请重新刷新获取");
                nodeToModifyInNew.put("checkSession", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除token
     * 并删除对应keys
     *
     * @return "删除成功！"or"删除失败"
     */
    @Override
    public String deleteToken(token token) {
        try {
            String name = token.getName();
            String parent = selectFile();
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
                if (token.getAccess_token().startsWith("eyJhb") && deleteShareToken(token)) {
                    return "删除并销毁share_token成功！";
                }
                return "删除成功！";
            } else {
                log.info("token未找到: " + name);
                return "token未找到！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "删除失败";
    }

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
        if (systemSetting.getAutoToken_url().equals("default")) {
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://127.0.0.1" + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + sessionToken;
        } else {
            url = systemSetting.getAutoToken_url() + sessionToken;
        }
        log.info("将通过这个网址请求登录信息：" + url);
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", token.getUsername())
                    .addFormDataPart("password", token.getUserPassword())
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.info("Request failed: " + response.body().string().trim());
                    return null;
                }
                String responseContent = response.body().string();
                String resToken = null;
                try {
                    JSONObject jsonResponse = new JSONObject(responseContent);
                    resToken = jsonResponse.getString("session_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.code() == 200 && resToken != null && resToken.startsWith("eyJhb")) {
                    return resToken;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 自动Token方法
     * 通过api/auth/login2拿到refresh_token
     * 更换tokens.json里存储的Token
     * 账号为token.getUserName()
     * 密码为token.getUserPassword()
     */
    public String updateRefreshToken(token token) {
        String url;
        systemSetting systemSetting = systemService.selectSetting();
        if (systemSetting.getAutoToken_url().equals("default")) {
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://127.0.0.1" + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + refreshToken;
        } else {
            url = systemSetting.getAutoToken_url() + refreshToken;
        }
        log.info("将通过这个网址请求登录信息：" + url);
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", token.getUsername())
                    .addFormDataPart("password", token.getUserPassword())
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.info("Request failed: " + response.body().string().trim());
                    return null;
                }
                String responseContent = response.body().string();
                String resToken = null;
                try {
                    JSONObject jsonResponse = new JSONObject(responseContent);
                    log.info(jsonResponse.toString());
                    resToken = jsonResponse.getString("refresh_token");
                    log.info(resToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.code() == 200) {
                    return resToken;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getAccessToken(token token) {
        try {
            String url;
            systemSetting systemSetting = systemService.selectSettingUrl();
            String tokenKind;
            String tokenName;
            if (token.isUseRefreshToken()) {
                tokenKind = reAccessToken;
                tokenName = "refresh_token";
            } else {
                tokenKind = accessToken;
                tokenName = "session_token";
            }
            if (systemSetting.getAutoToken_url().equals("default")) {
                String bingUrl = systemSetting.getBing();
                String[] parts = bingUrl.split(":");
                url = "http://127.0.0.1" + ":" + parts[1] + "/" + systemSetting.getProxy_api_prefix() + tokenKind;
            } else {
                url = systemSetting.getAutoToken_url() + tokenKind;
            }
            log.info("将通过这个网址请求登录信息：" + url);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(tokenName, token.getToken())
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Request failed: " + response.body().string().trim());
                return null;
            }
            String responseContent = response.body().string();
            String resToken = null;
            try {
                JSONObject jsonResponse = new JSONObject(responseContent);
                resToken = jsonResponse.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (response.code() == 200 && resToken != null && resToken.length() > 400) {
                return resToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getShareToken(token token) {
        systemSetting systemSetting = systemService.selectSettingUrl();
        String url = systemSetting.getAutoToken_url().equals("default")
                ? "http://127.0.0.1:" + systemSetting.getBing().split(":")[1] + "/" + systemSetting.getProxy_api_prefix() + shareToken
                : systemSetting.getAutoToken_url() + shareToken;
        log.info("将通过这个网址请求登录信息：" + url);
        String data = "unique_name=" + token.getName() + "&access_token=" + token.getAccess_token() +
                "&expires_in=0&show_conversations=false&show_userinfo=" + token.isShow_user_info();
        RequestBody body = RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("share token failed: " + response.body().string().trim());
                return null;
            }
            String tokenKey = new JSONObject(response.body().string()).getString("token_key");
            if (tokenKey.matches("^(fk-|pk-).*")) {
                return tokenKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteShareToken(token token) {
        systemSetting systemSetting = systemService.selectSettingUrl();
        String url = systemSetting.getAutoToken_url().equals("default")
                ? "http://127.0.0.1:" + systemSetting.getBing().split(":")[1] + "/" + systemSetting.getProxy_api_prefix() + shareToken
                : systemSetting.getAutoToken_url() + shareToken;
        log.info("将通过这个网址请求登录信息：" + url);
        String data = "unique_name=" + token.getName() + "&access_token=" + token.getAccess_token() +
                "&expires_in=-1&show_conversations=false&show_userinfo=" + token.isShow_user_info();
        RequestBody body = RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("share token failed: " + response.body().string().trim());
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getPoolToken(String pool_token, String shareTokens) {
        systemSetting systemSetting = systemService.selectSettingUrl();
        String url = systemSetting.getAutoToken_url().equals("default")
                ? "http://127.0.0.1:" + systemSetting.getBing().split(":")[1] + "/" + systemSetting.getProxy_api_prefix() + poolToken
                : systemSetting.getAutoToken_url() + poolToken;
        log.info("将通过这个网址请求登录信息：" + url);
        try {
            RequestBody requestBody;
            if (shareTokens == "") {
                List<token> tokens = selectToken("");
                StringBuffer resToken = new StringBuffer();
                for (token token : tokens) {
                    if (token.getShare_token() != null && token.isSetPoolToken()) {
                        resToken.append(token.getShare_token() + "\n");
                    }
                }
                requestBody = new FormBody.Builder()
                        .add("share_tokens", resToken.toString())
                        .add("pool_token", pool_token)
                        .build();
            } else {
                requestBody = new FormBody.Builder()
                        .add("share_tokens", shareTokens)
                        .add("pool_token", pool_token)
                        .build();
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.info("Request failed: " + response.body().string().trim());
                    return null;
                }
                String responseContent = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseContent);
                String resPoolToken = jsonResponse.getString("pool_token");
                log.info("一共运行了" + jsonResponse.getString("count") + "条share_token");
                if (resPoolToken.contains("pk")) {
                    log.info("pool_token更新为：" + resPoolToken);
                    return resPoolToken;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 生成shareToken和accessToken
     *
     * @return "生成成功" or "生成失败"
     */
    @Override
    public token autoUpdateSimpleToken(token token) {
        if (token == null || !token.isSetPoolToken()) {
            log.info("未查询到相关数据");
            return null;
        }
        String access_token = getAccessToken(token);
        if (access_token != null) {
            token.setAccess_token(access_token);
            //执行获取share_token操作
            String share_token = getShareToken(token);
            token.setShare_token(share_token);
            if (share_token != null) {
                token.setCheckSession(true);
                if (token.isAuto_shared()) {
                    token.setShared(true);
                }
                String res = product_requireToken(token, false);
                if (res.contains("成功")) {
                    log.info(res + "，修改share_token为：" + share_token);
                    return token;
                }
            }
        } else {
            token.setCheckSession(false);
            token.setShared(false);
            String res = product_requireToken(token, false);
            if (res.contains("成功")) {
                log.info("已为您禁用该session_token!");
            }
        }
        return null;
    }

    /**
     * 自动更新Token
     * 更换tokensTool里存储的Token的access_token和share_token
     * 并自动检查session是否过期
     *
     * @return "更新成功" or "更新失败"
     */
    public String autoUpdateToken(String name) {
        List<token> resTokens = selectToken(name);
        int newToken = 0;
        int allToken = 0;
        try {
            for (token token : resTokens) {
                if (!token.isSetPoolToken()) {
                    continue;
                }
                token res = autoUpdateSimpleToken(token);
                if (res != null) {
                    newToken++;
                }
                allToken++;
                Thread.sleep(1000);
            }
            if (newToken == 0) {
                log.info("自动生成Token失败！");
                return "自动生成Token失败！";
            } else {
                log.info("自动生成Token成功：" + newToken + "session或refresh过期：" + (allToken - newToken));
                return "自动生成Token成功：" + newToken + "<br>session或refresh过期：" + (allToken - newToken);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自动更新session_token时间80天
     *
     * @param token
     * @return
     */
    @Override
    public String autoUpdateSessionToken(token token) {
        if (token == null) {
            log.info("未查询到相关数据");
            return null;
        }
        try {
            if (token.isUseRefreshToken()) {
                String refreshToken = updateRefreshToken(token);
                if (refreshToken != null) {
                    return refreshToken;
                }
            } else {
                String sessionToken = updateSessionToken(token);
                if (sessionToken != null) {
                    return sessionToken;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String deletePoolToken(String pool_token) {
        String url;
        systemSetting systemSetting = systemService.selectSettingUrl();
        if (systemSetting.getAutoToken_url().equals("default")) {
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            url = "http://127.0.0.1" + ":" + parts[1] + "/" +
                    systemSetting.getProxy_api_prefix() + poolToken;
        } else {
            url = systemSetting.getAutoToken_url() + poolToken;
        }
        log.info("将通过这个网址请求登录信息：" + url);
        try {
            RequestBody requestBody = new FormBody.Builder()
                    .add("share_tokens", "")
                    .add("pool_token", pool_token)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String responseContent = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseContent);
                if (!response.isSuccessful() && !jsonResponse.has("detail")) {
                    log.info("Request failed: " + response.body().string().trim());
                    return null;
                }
                String resPoolToken = jsonResponse.getString("detail");
                if (response.code() == 200 && resPoolToken.length() > 0) {
                    log.info("注销pool_token成功!");
                    return resPoolToken;
                } else if (response.code() == 400) {
                    return "不存在该pool_token";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PandoraLimit getPandoraLimit() {
        systemSetting systemSetting = systemService.selectSettingLicense();
        String url = "https://dash.pandoranext.com/api/" + systemSetting.getLicense_id() + "/usage";
        log.info("将通过这个网址请求PandoraNext余额信息：" + url);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.info("Request failed: " + response.body().string().trim());
                    return null;
                }
                String responseContent = response.body().string();
                PandoraLimit pandoraLimit = new PandoraLimit();
                JSONObject jsonResponse = new JSONObject(responseContent);
                //用量
                pandoraLimit.setCurrent(jsonResponse.getInt("current"));
                //ip
                pandoraLimit.setIp(jsonResponse.getString("ip"));
                //总额
                pandoraLimit.setTotal(jsonResponse.getInt("total"));
                //重载时间
                pandoraLimit.setTtl(jsonResponse.getInt("ttl"));
                if (response.code() == 200 && pandoraLimit.toString().length() > 0) {
                    return pandoraLimit;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateSession() {
        try {
            List<token> tokens = selectToken("");
            token resToken = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime minDateTime = LocalDateTime.MAX;
            for (token token : tokens) {
                //保证refresh_token不被刷新
                if (token.isSetPoolToken() && !token.isUseRefreshToken()) {
                    LocalDateTime currentDateTime = LocalDateTime.parse(token.getUpdateTime(), formatter);
                    if (currentDateTime.isBefore(minDateTime)) {
                        minDateTime = currentDateTime;
                        resToken = token;
                    }
                }
            }
            token s = updateSession(resToken);
            if (s != null) {
                log.info("更新session_token,access_token和share_token成功！");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 刷新session，更新access_token和share_token
     *
     * @param token
     * @return
     */
    @Override
    public token updateSession(token token) {
        String res = autoUpdateSessionToken(token);
        if (res != null) {
            token.setToken(res);
            token.setSetPoolToken(true);
            token.setCheckSession(true);
            try {
                token resToken = autoUpdateSimpleToken(token);
                if (resToken != null) {
                    return resToken;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}