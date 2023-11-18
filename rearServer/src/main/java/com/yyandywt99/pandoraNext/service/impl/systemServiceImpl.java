package com.yyandywt99.pandoraNext.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yyandywt99.pandoraNext.pojo.token;
import com.yyandywt99.pandoraNext.service.systemService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Yangyang
 * @create 2023-11-18 9:54
 */
@Service
public class systemServiceImpl implements systemService {

    /**
     * 修改config.json里的系统值
     * @return "修改成功！"or"修改失败"
     */
    @Override
    public String requiredToken(token tem){
        try {
            String projectRoot = System.getProperty("user.dir");
            String parent = projectRoot + File.separator + "config.json";
            File jsonFile = new File(parent);
            Path jsonFilePath = Paths.get(parent);
            // 如果 JSON 文件不存在，创建一个新的 JSON 对象
            if (!jsonFile.exists()) {
                // 创建文件
                Files.createFile(jsonFilePath);
                System.out.println("tokens.json创建完成: " + jsonFilePath);
            }

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
                log.info(tem.isPlus()+"");
                nodeToModifyInNew.put("plus", tem.isPlus());
                // 检查是否需要 TokenPassword
                if (tem.getPassword() != null && tem.getPassword().length() > 0) {
                    nodeToModifyInNew.put("password", tem.getPassword());
                }
                else {
                    nodeToModifyInNew.put("password", "");
                }
                LocalDateTime now = LocalDateTime.now();
                nodeToModifyInNew.put("updateTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                // 将修改后的 newObjectNode 写回文件
                objectMapper.writeValue(new File(parent), newObjectNode);
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
}
