package com.yyandywt99.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public List<token> seleteToken(String name) {
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
                    temRes.setUserPassword(temNode.has("userPassword") ? temNode.get("userPassword").asText() : "");
                    temRes.setShared(temNode.has("shared") ? temNode.get("shared").asBoolean() : false);
                    temRes.setShow_user_info(temNode.has("show_user_info") ? temNode.get("show_user_info").asBoolean() : false);
                    temRes.setPlus(temNode.has("plus") ? temNode.get("plus").asBoolean() : false);
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
            res = updateToken(token);
            if (res != null) {
                token.setToken(res);
            } else {
                return "添加失败,检查你的账号密码是否正确或FakeOpen服务异常";
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
            newData.put("username", token.getUsername());
            newData.put("userPassword", token.getUserPassword());
            newData.put("shared", token.isShared());
            newData.put("show_user_info", token.isShow_user_info());
            newData.put("plus", token.isPlus());
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

                // 修改节点的值
                nodeToModifyInNew.put("token", tem.getToken());
                nodeToModifyInNew.put("username", tem.getUsername());
                nodeToModifyInNew.put("userPassword", tem.getUserPassword());
                nodeToModifyInNew.put("shared", tem.isShared());
                nodeToModifyInNew.put("show_user_info", tem.isShow_user_info());
                log.info(tem.isPlus() + "");
                nodeToModifyInNew.put("plus", tem.isPlus());
                // 检查是否需要 TokenPassword
                if (tem.getPassword() != null && tem.getPassword().length() > 0) {
                    nodeToModifyInNew.put("password", tem.getPassword());
                } else {
                    nodeToModifyInNew.put("password", "");
                }
                LocalDateTime now = LocalDateTime.now();
                nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                log.info("修改成功");
                return "修改成功！";
            } else {
                System.out.println("Node not found or not an object: " + nodeNameToModify);
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


    /**
     * 自动更新Token方法
     * 通过https://ai.fakeopen.com/auth/login拿到token
     * 更换tokens.json里存储的Token
     * 账号为token.getUserName()
     * 密码为token.getUserPassword()
     */
    public String updateToken(token token) {
        String url = "https://ai.fakeopen.com/auth/login";
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
            String access_token = null;
            try {
                JSONObject jsonResponse = new JSONObject(responseContent);
                // 提取返回的数据
                log.info(jsonResponse.toString());
                access_token = jsonResponse.getString("access_token");
                httpClient.close();
            } catch (JSONException e) {
                e.printStackTrace();
                httpClient.close();
            }
            //关闭进程
            if (statusCode == 200 && access_token.length() > 400) {
                log.info("Request was successful");
                //用来防止请求的token出现问题，回退token值
                return access_token;
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
    public String autoUpdateSimpleToken(token token) {
        if (token == null) {
            log.info("未查询到相关数据");
            return "更新失败，token为空！";
        }
        String temRes = updateToken(token);
        if (temRes != null) {
            String temToken = token.getToken();
            token.setToken(temRes);
            //执行修改token操作
            if (requiredToken(token).equals("修改成功！")) {
                return temRes;
            } else {
                //否则回退
                token.setToken(temToken);
            }
        }
        return "更新失败";
    }

    /**
     * 自动更新Token
     * 更换fakeApiTool里存储的Token
     * 更换One-API相应的FakeAPI
     *
     * @return "更新成功" or "更新失败"
     */
    public String autoUpdateToken(String name) {
        List<token> resTokens = seleteToken(name);
        int newToken = 0;
        for (token token : resTokens) {
            String temRes = updateToken(token);
            if (temRes != null) {
                token.setToken(temRes);
                //执行修改token操作
                if (requiredToken(token).equals("修改成功！")) {
                    newToken++;
                    try {
                        // 休息1000 毫秒 = 1 秒
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
}