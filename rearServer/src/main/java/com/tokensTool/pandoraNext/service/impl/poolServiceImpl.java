package com.tokensTool.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tokensTool.pandoraNext.pojo.poolToken;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.poolService;
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
 * @create 2023-12-10 11:59
 */
@Service
@Slf4j
public class poolServiceImpl implements poolService {
    private static final String gpt3Models = "gpt-3.5-turbo";

    private static final String gpt4Models = "gpt-3.5-turbo,gpt-4";

    private static final String openAiChat = "/v1/chat/completions";
    private static final String oneApiSelect = "api/channel/?p=0";
    private static final String oneAPiChannel = "api/channel/";
    private final String deploy = "default";
    @Value("${deployPosition}")
    private String deployPosition;
    @Autowired
    private apiServiceImpl apiService;
    @Autowired
    private systemServiceImpl systemService;

    /**
     * 遍历文件
     *
     * @return
     */
    public String selectFile() {
        String projectRoot;
        if (deploy.equals(deployPosition)) {
            projectRoot = System.getProperty("user.dir");
        } else {
            projectRoot = deployPosition;
        }
        String parent = projectRoot + File.separator + "pool.json";
        File jsonFile = new File(parent);
        Path jsonFilePath = Paths.get(parent);
        // 如果 JSON 文件不存在，创建一个新的 JSON 对象
        if (!jsonFile.exists()) {
            try {
                // 创建文件pool.json
                Files.createFile(jsonFilePath);
                // 往 pool.json 文件中添加一个空数组，防止重启报错
                Files.writeString(jsonFilePath, "{}");
                log.info("新建pool.json，并初始化pool.json成功！");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parent;
    }

    /**
     * 初始化pool.json
     * 添加checkPool变量,初始化为true
     * 启动的时候自动全部添加
     */
    public String initializeCheckPool() {
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
                        if (!nodeToModifyInNew.has("checkPool")) {
                            nodeToModifyInNew.put("checkPool", true);
                            log.info("为节点 " + nodeName + " 添加 checkPool 变量成功！");
                        }
                        // 初始化intoOneApi的值为false
                        if (!nodeToModifyInNew.has("intoOneApi")) {
                            nodeToModifyInNew.put("intoOneApi", false);
                            log.info("为节点 " + nodeName + " 添加 intoOneApi 变量成功！");
                        }
                        // 初始化pandoraNextGpt4的值为false
                        if (!nodeToModifyInNew.has("pandoraNextGpt4")) {
                            nodeToModifyInNew.put("pandoraNextGpt4", false);
                            log.info("为节点 " + nodeName + " 添加 pandoraNextGpt4 变量成功！");
                        }
                        // 初始化oneApi_pandoraUrl的值为""
                        if (!nodeToModifyInNew.has("oneApi_pandoraUrl")) {
                            nodeToModifyInNew.put("oneApi_pandoraUrl", "");
                            log.info("为节点 " + nodeName + " 添加 oneApi_pandoraUrl 变量成功！");
                        }
                        // 将修改后的 newObjectNode 写回文件
                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                    }
                }
                return "为所有子节点添加 checkPool 变量成功！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "为所有子节点添加 checkPool 变量失败！";
    }

    /**
     * 通过name遍历poolToken
     *
     * @param name
     * @return
     */
    public List<poolToken> selectPoolToken(String name) {
        List<poolToken> res = new ArrayList<>();
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
                    poolToken temRes = new poolToken();
                    temRes.setPoolName(nodeName);
                    // 获取对应的节点
                    JsonNode temNode = rootNode.get(nodeName);
                    temRes.setPoolToken(temNode.has("poolToken") ? temNode.get("poolToken").asText() : "");
                    temRes.setPoolTime(temNode.has("poolTime") ? temNode.get("poolTime").asText() : "");
                    // 将 JsonNode 转换为 List<String>
                    List<String> sharedTokens = new ArrayList<>();
                    if (temNode.has("shareTokens") && temNode.get("shareTokens").isArray()) {
                        for (JsonNode tokenNode : temNode.get("shareTokens")) {
                            sharedTokens.add(tokenNode.asText());
                        }
                    }
                    temRes.setShareTokens(sharedTokens);
                    temRes.setCheckPool(!temNode.has("checkPool") || temNode.get("checkPool").asBoolean());
                    //0.5.0
                    temRes.setIntoOneApi(temNode.has("intoOneApi") && temNode.get("intoOneApi").asBoolean());
                    temRes.setPandoraNextGpt4(temNode.has("pandoraNextGpt4") && temNode.get("pandoraNextGpt4").asBoolean());
                    temRes.setOneApi_pandoraUrl(temNode.has("oneApi_pandoraUrl") ? temNode.get("oneApi_pandoraUrl").asText() : "");
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
     * 仅支持修改poolToken的时间和值
     * 修改poolToken的时间
     * 修改poolToken的值
     *
     * @param poolToken
     * @return 修改成功！or 节点未找到或不是对象！
     * @throws Exception
     */
    public String requirePoolToken(poolToken poolToken) {
        try {
            String parent = selectFile();
            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));

            // 要修改的节点名称
            String nodeNameToModify = poolToken.getPoolName();

            // 获取要修改的节点
            JsonNode nodeToModify = rootNode.get(nodeNameToModify);

            if (nodeToModify != null && nodeToModify.isObject()) {
                // 创建新的 ObjectNode，并复制原始节点内容
                ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                newObjectNode.setAll((ObjectNode) rootNode);

                // 获取要修改的节点
                ObjectNode nodeToModifyInNew = newObjectNode.with(nodeNameToModify);

                //仅支持修改poolToken的时间和值
                LocalDateTime now = LocalDateTime.now();
                nodeToModifyInNew.put("checkPool", poolToken.isCheckPool());
                nodeToModifyInNew.put("poolToken", poolToken.getPoolToken());
                nodeToModifyInNew.put("poolTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                log.info("修改成功!");
                return "修改成功！";
            } else {
                log.info("节点未找到或不是对象,请检查pool.json！ " + nodeNameToModify);
                return "节点未找到或不是对象！";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String requireCheckPoolToken(poolToken poolToken) {
        try {
            String parent = selectFile();
            ObjectMapper objectMapper = new ObjectMapper();
            // 读取JSON文件并获取根节点
            JsonNode rootNode = objectMapper.readTree(new File(parent));
            // 要修改的节点名称
            String nodeNameToModify = poolToken.getPoolName();
            // 获取要修改的节点
            JsonNode nodeToModify = rootNode.get(nodeNameToModify);
            if (nodeToModify != null && nodeToModify.isObject()) {
                // 创建新的 ObjectNode，并复制原始节点内容
                ObjectNode newObjectNode = JsonNodeFactory.instance.objectNode();
                newObjectNode.setAll((ObjectNode) rootNode);
                // 获取要修改的节点
                ObjectNode nodeToModifyInNew = newObjectNode.with(nodeNameToModify);
                //仅支持修改poolToken的时间和值
                LocalDateTime now = LocalDateTime.now();
                nodeToModifyInNew.put("checkPool", poolToken.isCheckPool());
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                return "修改成功！";
            } else {
                log.info("节点未找到或不是对象,请检查pool.json！ " + nodeNameToModify);
                return "节点未找到或不是对象！";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过poolToken添加PoolToken
     *
     * @param poolToken
     * @return
     */
    public String addPoolToken(poolToken poolToken) {
        String resPoolToken;
        try {
            String shareTokens = getShareTokens(poolToken.getShareTokens());
            String temPoolToken = poolToken.getPoolToken();
            if (temPoolToken != null && temPoolToken.contains("pk")) {
                resPoolToken = apiService.getPoolToken(temPoolToken, shareTokens);
            } else {
                resPoolToken = apiService.getPoolToken("", shareTokens);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try {
            if (resPoolToken == null) {
                return "pool_token数据添加失败，请先按全部选择并生成，并确保url配对正确！";
            }
            poolToken.setPoolToken(resPoolToken);
            if (poolToken.isIntoOneApi()) {
                String[] strings = systemService.selectOneAPi();
                boolean b = addKey(poolToken, strings);
                if (b && poolToken.getPriority() != 0) {
                    boolean b1 = getPriority(poolToken, strings);
                    if (b1) {
                        log.info("修改优先级成功！");
                    }
                }
                if (b) {
                    log.info("pool_token进one-Api成功！");
                } else {
                    return "pool_token添加进one-api失败！";
                }
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
                System.out.println("pool.json创建完成: " + jsonFilePath);
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
            newData.put("poolToken", resPoolToken);
            List<String> shareTokensList = poolToken.getShareTokens();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (String value : shareTokensList) {
                arrayNode.add(value);
            }
            newData.set("shareTokens", arrayNode);
            //0.5.0
            newData.put("checkPool", true);
            newData.put("intoOneApi", poolToken.isIntoOneApi());
            newData.put("pandoraNextGpt4", poolToken.isPandoraNextGpt4());
            newData.put("oneApi_pandoraUrl", poolToken.getOneApi_pandoraUrl());

            LocalDateTime now = LocalDateTime.now();
            newData.put("poolTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 将新数据添加到 JSON 树中
            rootNode.put(poolToken.getPoolName(), newData);
            // 将修改后的数据写回到文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
            log.info("数据成功添加到 JSON 文件中。");
            return "pool_token数据添加成功";
        } catch (IOException e) {
            e.printStackTrace();
            return "添加失败！";
        }
    }

    /**
     * 删除通过poolToken里的poolName删除poolToken
     *
     * @param poolToken
     * @return
     */
    public String deletePoolToken(poolToken poolToken) {
        try {
            String name = poolToken.getPoolName();
            String parent = selectFile();
            String deletePoolToken = poolToken.getPoolToken();
            //确保注销成功！
            if (deletePoolToken != null && deletePoolToken.contains("pk")) {
                String s = apiService.deletePoolToken(deletePoolToken);
                if (s == null) {
                    log.info("删除失败，看看自己的poolToken是否合法");
                }
            }
            if (poolToken.isIntoOneApi()) {
                String[] strings = systemService.selectOneAPi();
                boolean b = deleteKeyId(poolToken, strings);
                if (!b) {
                    return "删除oneApi中的poolToken失败！";
                }
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


    /**
     * 从poolToken里拿到share_tokens的集合，传参给share_token
     *
     * @param shareName
     * @return
     */
    public String getShareTokens(List<String> shareName) {
        try {
            StringBuffer resToken = new StringBuffer();
            List<token> tokens = apiService.selectToken("");
            HashMap<String, String> tokensHashMap = new HashMap<>();
            for (token tem : tokens) {
                if (tem.isSetPoolToken() && tem.isCheckSession()) {
                    tokensHashMap.put(tem.getName(), tem.getShare_token());
                }
            }
            for (String temShareName : shareName) {
                try {
                    String temShareToken = tokensHashMap.get(temShareName);
                    String regex = "fk-[0-9a-zA-Z_\\-]{43}";
                    if (temShareToken != null && Pattern.matches(regex, temShareToken)) {
                        resToken.append(temShareToken + "\n");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return resToken.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 重新更新poolToken
     *
     * @return
     */
    public String refreshAllTokens() {
        log.info("开始自动更新PoolToken..........................");
        List<poolToken> poolTokens = selectPoolToken("");
        int count = 0;
        for (poolToken token : poolTokens) {
            String poolToken = token.getPoolToken();
            String shareToken = getShareTokens(token.getShareTokens());
            String resPoolToken = apiService.getPoolToken(poolToken, shareToken);
            if (resPoolToken != null && resPoolToken.equals(poolToken)) {
                token.setPoolToken(resPoolToken);
                if (requirePoolToken(token).contains("成功")) {
                    count++;
                }
            }
        }
        log.info("pool_token刷新成功:" + count + "，失败:" + (poolTokens.size() - count));
        return ("<br>pool_token刷新成功:" + count + "，失败:" + (poolTokens.size() - count));
    }

    /**
     * 手动单个更新poolToken
     *
     * @param poolToken
     * @return
     */
    public String refreshSimplyToken(poolToken poolToken) {
        try {
            List<poolToken> poolTokens = selectPoolToken("");
            for (poolToken token : poolTokens) {
                if (token.getPoolName().equals(poolToken.getPoolName())) {
                    String poolTokenValue = token.getPoolToken();
                    String shareToken = getShareTokens(token.getShareTokens());
                    log.info(shareToken);
                    String resPoolToken = apiService.getPoolToken(poolTokenValue, shareToken);
                    if (resPoolToken != null && resPoolToken.equals(poolTokenValue)) {
                        poolToken.setCheckPool(true);
                        if (requirePoolToken(poolToken).contains("成功")) {
                            return "刷新pool_token成功";
                        }
                    }
                }
            }
            return "没有找到该pool_token,刷新失败！";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "刷新失败！";
    }

    /**
     * 手动单个更改poolToken
     *
     * @param poolToken
     * @return
     */
    public String changePoolToken(poolToken poolToken) {
        try {
            String deletePoolToken = poolToken.getPoolToken();
            if (deletePoolToken != null && deletePoolToken.contains("pk")) {
                String s = null;
                try {
                    s = apiService.deletePoolToken(poolToken.getPoolToken());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (s == null) {
                    return "注销poolToken失败";
                }
            }
            String res = null;
            try {
                String resPoolToken = apiService.getPoolToken("", getShareTokens(poolToken.getShareTokens()));
                if (poolToken.isIntoOneApi()) {
                    String[] strings = systemService.selectOneAPi();
                    boolean temkeyId = deleteKeyId(poolToken, strings);
                    if (!temkeyId) {
                        return "删除oneAPi里的poolToken失败！";
                    } else {
                        try {
                            poolToken.setPoolToken(resPoolToken);
                            poolToken.setCheckPool(true);
                            res = requirePoolToken(poolToken);
                            boolean b = addKey(poolToken, strings);
                            if (b && res.contains("成功")) {
                                return res;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                poolToken.setPoolToken(resPoolToken);
                // 恢复正常
                poolToken.setCheckPool(true);
                res = requirePoolToken(poolToken);
                return res;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查全部pool_token
     * 是否过期或者出现问题
     */
    public String verifyAllPoolToken() {
        try {
            String openAiUrl = getOpenaiUrl() + openAiChat;
            List<poolToken> poolTokens = selectPoolToken("");
            int count = 0;
            for (poolToken poolToken : poolTokens) {
                String res = verifyPoolToken(poolToken, openAiUrl);
                if (res.contains("请确保")) {
                    return res;
                } else if (res != null && res.contains("正常")) {
                    count++;
                }
            }
            return "poolToken验证成功：" + count + "，失败：" + (poolTokens.size() - count);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toRequirePoolToken(poolToken poolToken) {
        String[] strings = systemService.selectOneAPi();
        deleteKeyId(poolToken, strings);
        String resPoolToken;
        try {
            String shareTokens = getShareTokens(poolToken.getShareTokens());
            String temPoolToken = poolToken.getPoolToken();
            if (temPoolToken != null && temPoolToken.contains("pk")) {
                resPoolToken = apiService.getPoolToken(temPoolToken, shareTokens);
            } else {
                resPoolToken = apiService.getPoolToken("", shareTokens);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try {
            if (resPoolToken == null) {
                return "pool_token数据添加失败，请先按全部选择并生成，并确保url配对正确！";
            }
            poolToken.setPoolToken(resPoolToken);
            if (poolToken.isIntoOneApi()) {
                boolean b = addKey(poolToken, strings);
                if (poolToken.getPriority() != 0) {
                    boolean b1 = getPriority(poolToken, strings);
                    if (b1) {
                        log.info("修改优先级成功！");
                    }
                }
                if (b) {
                    log.info("pool_token进one-Api成功！");
                } else {
                    return "pool_token添加进one-api失败！";
                }
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
                System.out.println("pool.json创建完成: " + jsonFilePath);
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
            newData.put("poolToken", resPoolToken);
            List<String> shareTokensList = poolToken.getShareTokens();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (String value : shareTokensList) {
                arrayNode.add(value);
            }
            newData.set("shareTokens", arrayNode);
            //0.5.0
            newData.put("checkPool", true);
            newData.put("intoOneApi", poolToken.isIntoOneApi());
            newData.put("pandoraNextGpt4", poolToken.isPandoraNextGpt4());
            newData.put("oneApi_pandoraUrl", poolToken.getOneApi_pandoraUrl());

            LocalDateTime now = LocalDateTime.now();
            newData.put("poolTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            // 将新数据添加到 JSON 树中
            rootNode.put(poolToken.getPoolName(), newData);
            // 将修改后的数据写回到文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
            log.info("数据成功添加到 JSON 文件中。");
            return "pool_token数据修改成功";
        } catch (IOException e) {
            e.printStackTrace();
            return "修改失败！";
        }
    }

    /**
     * 检查单个pool_token
     * 是否过期或者出现问题
     */
    @Override
    public String verifySimplyPoolToken(poolToken poolToken) {
        try {
            String openAiUrl = getOpenaiUrl();
            String res = verifyPoolToken(poolToken, openAiUrl + openAiChat);
            if (res != null) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查pool_token
     * 是否过期或者出现问题
     */
    public String verifyPoolToken(poolToken poolToken, String url) {
        OkHttpClient client = new OkHttpClient();

        // 构造请求体，JSON格式，包含一个字符串参数prompt和一个整数参数max_tokens，如果有其他参数，延续即可。
        String json = "{" +
                "    \"model\": \"gpt-3.5-turbo\"," +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"Say this is a test!\"}]," +
                "    \"temperature\": 0.7" +
                "}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + poolToken.getPoolToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() && response.code() == 404) {
                return "请检查PandoraNext公网地址是否填写正确！";
            }
            String result = response.body().string();
            JSONObject jsonResponse = new JSONObject(result);
            if (!jsonResponse.has("choices")) {
                poolToken.setCheckPool(false);
                String s = requireCheckPoolToken(poolToken);
                if (s.contains("成功")) {
                    return "pool_token过期，请重新刷新，" + s;
                } else {
                    log.info("已为你自动标记过期poolToken!");
                    return "pool_token过期，请重新刷新！";
                }
            }
            // 提取返回的数据
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            JSONObject firstChoiceObject = choicesArray.getJSONObject(0);
            JSONObject messageObject = firstChoiceObject.getJSONObject("message");
            String content = messageObject.getString("content");
            poolToken.setCheckPool(true);
            String s = requireCheckPoolToken(poolToken);
            return "pool_token正常，请放心使用！";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getOpenaiUrl() {
        try {
            systemSetting systemSetting = systemService.selectSetting();
            String pandoraNextOutUrl = systemSetting.getPandoraNext_outUrl();
            String proxyApiPrefix = systemSetting.getProxy_api_prefix();
            if (pandoraNextOutUrl.charAt(pandoraNextOutUrl.length() - 1) != '/') {
                pandoraNextOutUrl += "/";
            }
            return pandoraNextOutUrl + proxyApiPrefix;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 添加Key值
     * 会通过Post方法访问One-Api接口/api/channel/,添加新keys
     *
     * @return "true"or"false"
     */
    public boolean addKey(poolToken addKeyPojo, String[] systemSetting) {
        if (!addKeyPojo.isIntoOneApi()) {
            return false;
        }
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneAPiChannel
                : systemSetting[0] + "/" + oneAPiChannel;
        log.info(url);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 8);
            jsonObject.put("key", addKeyPojo.getPoolToken());
            jsonObject.put("name", addKeyPojo.getPoolName());
            jsonObject.put("base_url", getOpenaiUrl());
            jsonObject.put("other", "");
            if (addKeyPojo.isPandoraNextGpt4()) {
                jsonObject.put("models", gpt4Models);
            } else {
                jsonObject.put("models", gpt3Models);
            }
            String group = addKeyPojo.getGroupChecked();
            jsonObject.put("group", group);
            jsonObject.put("model_mapping", "");
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
                    log.info("请求one-api失败，失败码: " + response.code());
                    return false;
                }
                String responseContent = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseContent);
                boolean success = jsonResponse.getBoolean("success");
                if (response.code() == 200 && success) {
                    return true;
                } else {
                    log.info("请求one-api失败，失败码: " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean deleteKeyId(poolToken poolToken, String[] systemSetting) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneApiSelect
                : systemSetting[0] + "/" + oneApiSelect;
        log.info(url);
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + systemSetting[1])
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.info("浏览器状态为： " + response.code());
                    return false;
                }
                String responseContent = response.body().string();
                JSONObject jsonObject = new JSONObject(responseContent);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                int id = -1;
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String name = dataObject.getString("name");
                    if (name.equals(poolToken.getPoolName())) {
                        id = dataObject.getInt("id");
                        break;
                    }
                }
                if (response.code() == 200) {
                    if (id > 0) {
                        boolean res = deleteKey(systemSetting, id);
                        return res;
                    }
                    log.info("没有找到相应的key名!");
                    return true;
                } else {
                    log.info("浏览器状态为： " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean getPriority(poolToken poolToken, String[] systemSetting) {
        String url = systemSetting[0].endsWith("/") ? systemSetting[0] + oneApiSelect
                : systemSetting[0] + "/" + oneApiSelect;
        log.info(url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + systemSetting[1])
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.info("没有找到相应的key名，浏览器状态为： " + response.code());
                return false;
            }
            String responseContent = response.body().string();
            JSONObject jsonObject = new JSONObject(responseContent);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                String name = dataObject.getString("name");
                if (name.equals(poolToken.getPoolName())) {
                    int id = dataObject.getInt("id");
                    return priorityKey(systemSetting, id, poolToken.getPriority());
                }
            }
            log.info("没有找到相应的key名");
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

}
