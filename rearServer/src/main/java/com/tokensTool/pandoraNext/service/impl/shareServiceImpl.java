package com.tokensTool.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tokensTool.pandoraNext.pojo.shareToken;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.shareService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
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
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Yangyang
 * @create 2024-01-08 10:22
 */
@Service
@Slf4j
public class shareServiceImpl implements shareService {
    private static final String oneApiSelect = "api/channel/?p=0";
    private static final String oneAPiChannel = "api/channel/";
    private static final HashMap<String, String> share_tokenList;

    static {
        share_tokenList = new HashMap<>();
        log.info("初始化share_tokenList成功！");
    }

    private final String deploy = "default";
    @Value("${deployPosition}")
    private String deployPosition;
    @Autowired
    private apiServiceImpl apiService;
    @Autowired
    private systemServiceImpl systemService;

    public String selectFile() {
        String projectRoot;
        if (deploy.equals(deployPosition)) {
            projectRoot = System.getProperty("user.dir");
        } else {
            projectRoot = deployPosition;
        }
        String parent = projectRoot + File.separator + "share.json";
        File jsonFile = new File(parent);
        Path jsonFilePath = Paths.get(parent);
        // 如果 JSON 文件不存在，创建一个新的 JSON 对象
        if (!jsonFile.exists()) {
            try {
                // 创建文件share.json
                Files.createFile(jsonFilePath);
                // 往 share.json 文件中添加一个空数组，防止重启报错
                Files.writeString(jsonFilePath, "{}");
                log.info("新建share.json，并初始化share.json成功！");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parent;
    }

    /**
     * 通过name遍历shareToken
     *
     * @param name
     * @return
     */
    public List<shareToken> selectShareToken(String name) {
        List<shareToken> res = new ArrayList<>();
        try {
            String parent = selectFile();
            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));
            // 遍历所有字段
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String nodeName = entry.getKey();
                if (nodeName.contains(name)) {
                    shareToken temRes = new shareToken();
                    temRes.setOneApi_name(nodeName);
                    // 获取对应的节点
                    JsonNode temNode = rootNode.get(nodeName);
                    temRes.setToken_name(temNode.has("token_name") ? temNode.get("token_name").asText() : "");
                    temRes.setToken_value(temNode.has("token_value") ? temNode.get("token_value").asText() : "");
                    temRes.setOneApi_groups(temNode.has("oneApi_groups") ? temNode.get("oneApi_groups").asText() : "");
                    //0.5.0
                    temRes.setOneApi_models(temNode.has("oneApi_models") ? temNode.get("oneApi_models").asText() : "");
                    temRes.setModel_mapping(temNode.has("model_mapping") ? temNode.get("model_mapping").asText() : "");
                    temRes.setOneApi_baseUrl(temNode.has("oneApi_baseUrl") ? temNode.get("oneApi_baseUrl").asText() : "");
                    temRes.setShareTime(temNode.has("shareTime") ? temNode.get("shareTime").asText() : "");
                    temRes.setPriority(temNode.has("priority") ? temNode.get("priority").asInt() : 0);
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
     * 通过shareToken添加shareToken
     *
     * @param shareToken
     * @return
     */
    public String addShareToken(shareToken shareToken) {
        try {
            shareToken = getShareValue(shareToken);
            String[] strings = systemService.selectOneAPi();
            boolean b = addKey(shareToken, strings);
            if (b && shareToken.getPriority() != 0) {
                boolean b1 = getPriority(shareToken, strings);
                if (b1) {
                    log.info("修改优先级成功！");
                }
            }
            if (b) {
                log.info("share_token进one-Api成功！");
            } else {
                return "share_token添加进one-api失败！";
            }
            String parent = selectFile();
            File jsonFile = new File(parent);
            Path jsonFilePath = Paths.get(parent);
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode;
            // 如果 JSON 文件不存在，创建一个新的 JSON 对象
            if (!jsonFile.exists()) {
                // 创建文件
                Files.createFile(jsonFilePath);
                System.out.println("share.json创建完成: " + jsonFilePath);
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
            newData.put("token_name", shareToken.getToken_name());
            //0.5.0
            newData.put("token_value", shareToken.getToken_value());
            newData.put("oneApi_groups", shareToken.getOneApi_groups());
            newData.put("oneApi_models", shareToken.getOneApi_models());
            newData.put("model_mapping", shareToken.getModel_mapping());
            newData.put("oneApi_baseUrl", shareToken.getOneApi_baseUrl());
            newData.put("priority", shareToken.getPriority());

            LocalDateTime now = LocalDateTime.now();
            newData.put("shareTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 将新数据添加到 JSON 树中
            rootNode.put(shareToken.getOneApi_name(), newData);
            // 将修改后的数据写回到文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
            log.info("数据成功添加到 JSON 文件中。");
            share_tokenList.put(shareToken.getOneApi_name(), shareToken.getToken_value());
            return "share_token数据添加成功";
        } catch (IOException e) {
            e.printStackTrace();
            return "添加失败！";
        }
    }

    public shareToken getShareValue(shareToken shareToken) {
        if (shareToken == null) {
            return null;
        }
        List<token> tokens = apiService.selectToken("");
        for (token token : tokens) {
            if (token.getName().equals(shareToken.getToken_name())) {
                if (token.isSetPoolToken() && Pattern.matches("fk-[0-9a-zA-Z_\\-]{43}", token.getShare_token())) {
                    shareToken.setToken_value(token.getShare_token());
                }
            }
        }
        return shareToken;
    }

    /**
     * 添加Key值
     * 会通过Post方法访问One-Api接口/api/channel/,添加新keys
     *
     * @return "true"or"false"
     */
    public boolean addKey(shareToken addKeyPojo, String[] systemSetting) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneAPiChannel
                : systemSetting[0] + "/" + oneAPiChannel;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 8);
            jsonObject.put("key", addKeyPojo.getToken_value());
            jsonObject.put("name", addKeyPojo.getOneApi_name());
            jsonObject.put("base_url", addKeyPojo.getOneApi_baseUrl());
            jsonObject.put("other", "");
            jsonObject.put("models", addKeyPojo.getOneApi_models());
            String group = addKeyPojo.getOneApi_groups();
            jsonObject.put("group", group);
            jsonObject.put("model_mapping", addKeyPojo.getModel_mapping());
            jsonObject.put("groups", new JSONArray().put(group));
            // 将JSON对象转换为字符串
            String json = jsonObject.toString();

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + systemSetting[1])
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("请求one-api失败，失败码: " + response.code());
                    return false;
                }
                String responseContent = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseContent);
                boolean success = jsonResponse.getBoolean("success");
                if (response.code() == 200 && success) {
                    return true;
                } else {
                    log.error("请求one-api失败，失败码: " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteKeyId(shareToken shareToken, String[] systemSetting) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneApiSelect
                : systemSetting[0] + "/" + oneApiSelect;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + systemSetting[1])
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("浏览器状态为： " + response.code());
                    return false;
                }
                String responseContent = response.body().string();
                JSONObject jsonObject = new JSONObject(responseContent);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                int id = -1;
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String name = dataObject.getString("name");
                    if (name.equals(shareToken.getOneApi_name())) {
                        id = dataObject.getInt("id");
                        break;
                    }
                }
                if (response.code() == 200) {
                    if (id > 0) {
                        boolean res = deleteKey(systemSetting, id);
                        return res;
                    }
                    log.error("没有找到相应的key名!");
                    return true;
                } else {
                    log.error("浏览器状态为： " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getPriority(shareToken shareToken, String[] systemSetting) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneApiSelect
                : systemSetting[0] + "/" + oneApiSelect;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + systemSetting[1])
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("没有找到相应的key名，浏览器状态为： " + response.code());
                return false;
            }
            String responseContent = response.body().string();
            JSONObject jsonObject = new JSONObject(responseContent);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                String name = dataObject.getString("name");
                if (name.equals(shareToken.getOneApi_name())) {
                    int id = dataObject.getInt("id");
                    return priorityKey(systemSetting, id, shareToken.getPriority());
                }
            }
            log.error("没有找到相应的key名");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteKey(String[] systemSetting, int keyId) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneAPiChannel + keyId
                : systemSetting[0] + "/" + oneAPiChannel + keyId;
        log.info("请求one-api的网址为：" + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + systemSetting[1])
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("未找到当前的key，浏览器状态为: " + response.code());
                return false;
            }
            String responseContent = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseContent);
            boolean success = jsonResponse.getBoolean("success");
            if (success) {
                log.info("key删除成功！");
                return true;
            }
            log.info(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean priorityKey(String[] systemSetting, int keyId, Integer priority) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneAPiChannel
                : systemSetting[0] + "/" + oneAPiChannel;
        log.info("请求one-api的网址为：" + url);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", keyId);
            jsonObject.put("priority", priority);
            String json = jsonObject.toString();
            body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + systemSetting[1])
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseContent = Objects.requireNonNull(response.body()).string();
            JSONObject jsonResponse = new JSONObject(responseContent);
            if (response.isSuccessful() && jsonResponse.getBoolean("success")) {
                return true;
            } else {
                log.info("更改优先级失败，失败码: " + response.code());
            }
            log.info(jsonResponse.toString());
        } catch (Exception e) {
            log.error("请求处理异常", e);
        }
        return false;
    }

    public String requireShareToken(shareToken shareToken) {
        try {
            String res1 = deleteShareToken(shareToken);
            if (res1.contains("成功")) {
                String res2 = addShareToken(shareToken);
                if (res2.contains("成功")) {
                    return "修改share_token到oneapi成功";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改失败！";
    }

    public String deleteShareToken(shareToken shareToken) {
        try {
            String name = shareToken.getOneApi_name();
            String parent = selectFile();
            String[] strings = systemService.selectOneAPi();
            boolean b = deleteKeyId(shareToken, strings);
            if (!b) {
                return "删除oneApi中的shareToken失败！";
            }
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

    public String refreshAllToken() {
        try {
            int count = 0;
            int count_sus = 0;
            List<shareToken> shareTokens = selectShareToken("");
            for (shareToken shareToken : shareTokens) {
                String newShareToken = getshareToken(shareToken);
                log.info(newShareToken);
                if (!newShareToken.equals(share_tokenList.get(shareToken.getOneApi_name()))
                        && newShareToken != null) {
                    String res1 = deleteShareToken(shareToken);
                    if (res1.contains("成功")) {
                        String res2 = addShareToken(shareToken);
                        if (res2.contains("成功")) {
                            count++;
                        }
                    }
                } else {
                    count_sus++;
                    count++;
                    log.info(("share_token未发生变化，无需更新"));
                }
            }
            return "<br>share_tokens in oneApi刷新成功/未过期：" + count + "/" + count_sus + "，失败：" + (shareTokens.size() - count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "share_tokens in oneApi刷新失败！";
    }

    public String getshareToken(shareToken shareToken) {
        if (shareToken == null) {
            return null;
        }
        List<token> tokens = apiService.selectToken("");
        for (token token : tokens) {
            if (token.getName().equals(shareToken.getToken_name())) {
                return token.getShare_token();
            }
        }
        return null;
    }
}
