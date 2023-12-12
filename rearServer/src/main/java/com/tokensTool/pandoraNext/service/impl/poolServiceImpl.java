package com.tokensTool.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tokensTool.pandoraNext.pojo.poolToken;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.poolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

/**
 * @author Yangyang
 * @create 2023-12-10 11:59
 */
@Service
@Slf4j
public class poolServiceImpl implements poolService {

    @Value("${deployPosition}")
    private String deployPosition;
    private String deploy = "default";

    @Autowired
    private apiServiceImpl apiService;

    /**
     * 遍历文件
     * @return
     */
    public String selectFile(){
        String projectRoot;
        if(deploy.equals(deployPosition)){
            projectRoot = System.getProperty("user.dir");
        }
        else{
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
     * 通过name遍历poolToken
     * @param name
     * @return
     */
    public List<poolToken> selectPoolToken(String name) {
        List<poolToken> res = new ArrayList<>();
        try {
            String parent = selectFile();
            log.info(parent);
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
     * @param poolToken
     * @return 修改成功！or 节点未找到或不是对象！
     * @throws Exception
     */
    public String requirePoolToken(poolToken poolToken) {
        try {
            String parent = selectFile();
            log.info(parent);
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
                nodeToModifyInNew.put("poolToken", poolToken.getPoolToken());
                nodeToModifyInNew.put("poolTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                // 将修改后的 newObjectNode 写回文件
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(parent), newObjectNode);
                log.info("修改成功");
                return "修改成功！";
            } else {
                System.out.println("节点未找到或不是对象,请检查pool.json！ " + nodeNameToModify);
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
    public String addPoolToken(poolToken poolToken){
        String resPoolToken;
        try {
            String shareTokens = getShareTokens(poolToken.getShareTokens());
            String temPoolToken = poolToken.getPoolToken();
            if(temPoolToken != null && temPoolToken.contains("pk")){
                resPoolToken = apiService.getPoolToken(temPoolToken,shareTokens);
            }
            else {
                resPoolToken = apiService.getPoolToken("", shareTokens);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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

            newData.put("poolToken",resPoolToken);
            List<String> shareTokensList = poolToken.getShareTokens();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (String value : shareTokensList) {
                arrayNode.add(value);
            }
            newData.set("shareTokens", arrayNode);
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
     * @param poolToken
     * @return
     */
    public String deletePoolToken(poolToken poolToken) {
        try {
            String name = poolToken.getPoolName();
            String parent = selectFile();
            String deletePoolToken = poolToken.getPoolToken();
            log.info(parent);
            //确保注销成功！
            if(deletePoolToken != null && deletePoolToken.contains("pk")){
                String s = apiService.deletePoolToken(deletePoolToken);
                if(s == null){
                    log.info("删除失败，看看自己的poolToken是否合法");
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
     * @param shareName
     * @return
     */
    public String getShareTokens(List<String> shareName) {
        try {
            StringBuffer resToken = new StringBuffer();
            List<token> tokens = apiService.selectToken("");
            for (String temShareName : shareName) {
                for (token token : tokens) {
                    if(token.getName().equals(temShareName)
                            && token.getShare_token() != null
                            && token.isSetPoolToken()){
                        resToken.append(token.getShare_token()+"\n");
                    }
                }
            }
            return resToken.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 定时任务每五天后的凌晨4点10分更新poolToken
     * @return
     */

    @Scheduled(cron = "0 0 4 * * ?")
    public void refreshAllTokens(){
        log.info("开始自动更新PoolToken..........................");
        List<poolToken> poolTokens = selectPoolToken("");
        int count = 0;
        for(poolToken token : poolTokens){
            String poolToken = token.getPoolToken();
            String shareToken = getShareTokens(token.getShareTokens());
            String resPoolToken = apiService.getPoolToken(poolToken, shareToken);
            if(resPoolToken != null && resPoolToken.equals(poolToken)){
                token.setPoolToken(resPoolToken);
                if(requirePoolToken(token).contains("成功")){
                    count++;
                    try {
                        // 休眠一秒（1000毫秒）
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // 处理中断异常
                        e.printStackTrace();
                    }
                }
            }
        }
        log.info("pool_token刷新成功:"+count+"失败:"+ (poolTokens.size() - count));
    }

    /**
     * 手动单个更新poolToken
     * @param poolToken
     * @return
     */
    public String refreshSimplyToken(poolToken poolToken){
        try {
            List<poolToken> poolTokens = selectPoolToken("");
            for(poolToken token : poolTokens){
                if(token.getPoolName().equals(poolToken.getPoolName())){
                    String poolTokenValue = token.getPoolToken();
                    String shareToken = getShareTokens(token.getShareTokens());
                    String resPoolToken = apiService.getPoolToken(poolTokenValue, shareToken);
                    if(resPoolToken != null && resPoolToken.equals(poolTokenValue)){
                        if(requirePoolToken(poolToken).contains("成功")){
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
     * @param poolToken
     * @return
     */
    public String changePoolToken(poolToken poolToken){
        try {
            String deletePoolToken = poolToken.getPoolToken();
            if(deletePoolToken != null && deletePoolToken.contains("pk")) {
                String s = null;
                try {
                    s = apiService.deletePoolToken(poolToken.getPoolToken());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if(s == null){
                    return "注销poolToken失败";
                }
            }
            String res = null;
            try {
                String resPoolToken = apiService.getPoolToken("",getShareTokens(poolToken.getShareTokens()));
                poolToken.setPoolToken(resPoolToken);
                res = requirePoolToken(poolToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
