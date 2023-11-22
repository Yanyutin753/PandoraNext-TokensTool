package com.yyandywt99.pandoraNext.service.impl;

import com.yyandywt99.pandoraNext.pojo.systemSetting;
import com.yyandywt99.pandoraNext.service.systemService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-18 9:54
 */
@Slf4j
@Service
public class systemServiceImpl implements systemService {
    @Value("${deployPosition}")
    private String deployPosition;
    private String deploy = "default";

    public String selectFile(){
        String projectRoot;
        if(deploy.equals(deployPosition)){
            projectRoot = System.getProperty("user.dir");
        }
        else{
            projectRoot = deployPosition;
        }
        String parent = projectRoot + File.separator + "config.json";
        File jsonFile = new File(parent);
        Path jsonFilePath = Paths.get(parent);
        // 如果 JSON 文件不存在，创建一个新的 JSON 对象
        if (!jsonFile.exists()) {
            try {
                // 创建文件config.json
                Files.createFile(jsonFilePath);
                // 往 config.json 文件中添加一个空数组，防止重启报错
                Files.writeString(jsonFilePath, "{}");
                System.out.println("空数组添加完成");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("config.json创建完成: " + jsonFilePath);
        }
        return parent;
    }
    /**
     * 修改config.json里的系统值
     * @return "修改成功！"or"修改失败"
     */
    @Override
    public String requiredSetting(systemSetting tem){
        String parent = selectFile();
        try {
            // 读取 JSON 文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(parent)));

            JSONObject jsonObject = new JSONObject(jsonContent);
            if(tem.getBing() != null && tem.getBing().length() > 0){
                jsonObject.put("bind", tem.getBing());
            }
            if(tem.getTimeout() != null && tem.getTimeout().toString().length() > 0){
                jsonObject.put("timeout", tem.getTimeout());
            }
            if (tem.getProxy_url() != null && tem.getProxy_url().length() > 0) {
                jsonObject.put("proxy_url", tem.getProxy_url());
            }
            else {
                jsonObject.put("proxy_url", "");
            }
            if (tem.getPublic_share() != null && tem.getPublic_share().toString().length() > 0) {
                jsonObject.put("public_share", tem.getPublic_share());
            }
            if (tem.getSite_password() != null && tem.getSite_password().length() > 0) {
                jsonObject.put("site_password", tem.getSite_password());
            }
            else {
                jsonObject.put("site_password", "");
            }
            if (tem.getSetup_password() != null && tem.getSetup_password().length() > 0) {
                jsonObject.put("setup_password", tem.getSetup_password());
            }
            else {
                jsonObject.put("setup_password", "");
            }
            JSONArray jsonArray = null;
            if (tem.getWhitelist() != null && tem.getWhitelist().length() > 0 && tem.getWhitelist() != "null") {
                String numbersString = tem.getWhitelist().replaceAll("[\\[\\]]", "");
                // 使用逗号分割字符串得到数组
                String[] numbersArray = numbersString.split(",");
                // 将数组转换为 List<String>
                List<String> numbersList = new ArrayList<>(Arrays.asList(numbersArray));
                jsonArray = new JSONArray(numbersList);
                jsonObject.put("whitelist",jsonArray);
            }
            else {
                jsonObject.put("whitelist", JSONObject.NULL);
            }

            // 将修改后的 JSONObject 转换为格式化的 JSON 字符串
            String updatedJson = jsonObject.toString(2);
            Files.write(Paths.get(parent), updatedJson.getBytes());
            return "修改config.json成功，快去重启PandoraNext吧!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改config.json失败";
    }

    /**
     * 查询config.json里的系统值
     * @return systemSettings类
     */
    public systemSetting selectSetting(){
        String parent = selectFile();
        try {
            // 读取 JSON 文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(parent)));
            // 将 JSON 字符串解析为 JSONObject
            JSONObject jsonObject = new JSONObject(jsonContent);

            // 将 JSONObject 转换为 Config 类的实例
            systemSetting config = new systemSetting();
            config.setSite_password(jsonObject.optString("site_password"));
            config.setSetup_password(jsonObject.optString("setup_password"));
            config.setBing(jsonObject.optString("bind"));
            config.setPublic_share(jsonObject.optBoolean("public_share"));
            config.setProxy_url(jsonObject.optString("proxy_url"));
            config.setWhitelist(jsonObject.isNull("whitelist") ? null : jsonObject.optString("whitelist"));
            config.setTimeout(jsonObject.getInt("timeout"));
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
