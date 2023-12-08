package com.yyandywt99.pandoraNext.service.impl;

import com.yyandywt99.pandoraNext.pojo.systemSetting;
import com.yyandywt99.pandoraNext.pojo.tls;
import com.yyandywt99.pandoraNext.pojo.validation;
import com.yyandywt99.pandoraNext.service.systemService;
import com.yyandywt99.pandoraNext.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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
            updateJsonValue(jsonObject,"bind",tem.getBing());
            updateJsonValue(jsonObject,"timeout",tem.getTimeout());
            updateJsonValue(jsonObject,"proxy_url",tem.getProxy_url());
            updateJsonValue(jsonObject,"public_share",tem.getPublic_share());
            updateJsonValue(jsonObject,"site_password",tem.getSite_password());
            updateJsonValue(jsonObject,"setup_password",tem.getSetup_password());
            JSONArray jsonArray = null;
            if (tem.getWhitelist() != null && tem.getWhitelist().length() > 0 && tem.getWhitelist() != "null") {
                String numbersString = tem.getWhitelist().replaceAll("[\\[\\]]", "");
                String[] numbersArray = numbersString.split(",");
                // 将数组转换为 List<String>
                List<String> numbersList = new ArrayList<>(Arrays.asList(numbersArray));
                jsonArray = new JSONArray(numbersList);
                jsonObject.put("whitelist",jsonArray);
            }
            else {
                jsonObject.put("whitelist", JSONObject.NULL);
            }

            //4.7.2
            if(! tem.getLoginPassword().equals(jsonObject.optString("loginPassword"))
                    || ! tem.getLoginUsername().equals(jsonObject.optString("loginUsername"))){
                Instant instant = Instant.now();
                //时间戳
                String key = String.valueOf(instant.toEpochMilli());
                JwtUtils.setSignKey(key);
            }

            updateJsonValue(jsonObject,"loginUsername",tem.getLoginUsername());
            updateJsonValue(jsonObject,"loginPassword",tem.getLoginPassword());

            updateJsonValue(jsonObject,"license_id",tem.getLicense_id());
            updateJsonValue(jsonObject,"autoToken_url",tem.getAutoToken_url());
            updateJsonValue(jsonObject,"getTokenPassword",tem.getGetTokenPassword());
            updateJsonValue(jsonObject,"containerName",tem.getContainerName());

            updateJsonValue(jsonObject,"isolated_conv_title",tem.getIsolated_conv_title());
            updateJsonValue(jsonObject,"proxy_api_prefix",tem.getProxy_api_prefix());

            // 4.7
            updateJsonValue(jsonObject,"pool_token",tem.getPool_token());

            // validation
            validation validation = tem.getValidation();
            JSONObject captchaJson = jsonObject.getJSONObject("captcha");
            updateJsonValue(captchaJson, "provider", validation.getProvider());
            updateJsonValue(captchaJson, "site_key", validation.getSite_key());
            updateJsonValue(captchaJson, "site_secret", validation.getSite_secret());
            updateJsonValue(captchaJson, "site_login", validation.isSite_login());
            updateJsonValue(captchaJson, "setup_login", validation.isSetup_login());
            updateJsonValue(captchaJson, "oai_username", validation.isOai_username());
            updateJsonValue(captchaJson, "oai_password", validation.isOai_password());

            // tls
            tls tls = tem.getTls();
            JSONObject tlsJson = jsonObject.getJSONObject("tls");
            updateJsonValue(tlsJson, "enabled", tls.isEnabled());
            updateJsonValue(tlsJson, "cert_file", tls.getCert_file());
            updateJsonValue(tlsJson, "key_file", tls.getKey_file());

            // 将修改后的 JSONObject 转换为格式化的 JSON 字符串
            String updatedJson = jsonObject.toString(2);
            Files.write(Paths.get(parent), updatedJson.getBytes());
            return "修改config.json成功，快去重启PandoraNext吧!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改config.json失败";
    }

    private void updateJsonValue(JSONObject jsonObject, String key, Object value) {
        if(value == null || value.toString().length() == 0) {
            return;
        }
        try {
            if (value != null && value.toString().length() > 0) {
                jsonObject.put(key, value);
            }
            else if(value.toString().length() == 0) {
                jsonObject.put(key, "");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 查询config.json里的系统值
     * @return systemSettings类
     */
    public systemSetting selectSetting(){
        boolean exist = true;
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
            try {
                jsonObject.getString("loginUsername");
            } catch (JSONException e) {
                jsonObject.put("loginUsername", "root");
                log.info("config.json没有新增loginUsername参数,现已增加！");
                exist = false;
            }
            try {
                jsonObject.getString("loginPassword");
            } catch (JSONException e) {
                jsonObject.put("loginPassword", "123456");
                log.info("config.json没有新增loginPassword参数,现已增加！");
                exist = false;
            }
            try {
                jsonObject.getString("license_id");
            } catch (JSONException e) {
                jsonObject.put("license_id", "");
                log.info("config.json没有新增license_id参数,现已增加！");
                exist = false;
            }

            try {
                jsonObject.getString("autoToken_url");
            } catch (JSONException e) {
                jsonObject.put("autoToken_url", "default");
                log.info("config.json没有新增autoToken_url参数,现已增加！");
                exist = false;
            }
            try {
                jsonObject.getString("getTokenPassword");
            } catch (JSONException e) {
                jsonObject.put("getTokenPassword", "");
                log.info("config.json没有新增getTokenPassword参数,现已增加！");
                exist = false;
            }

            try {
                jsonObject.getString("containerName");
            } catch (JSONException e) {
                jsonObject.put("containerName", "PandoraNext");
                log.info("config.json没有新增containerName参数,现已增加！");
                exist = false;
            }

            try {
                jsonObject.getString("isolated_conv_title");
            } catch (JSONException e) {
                jsonObject.put("isolated_conv_title", "*");
                log.info("config.json没有新增isolated_conv_title参数,现已增加！");
                exist = false;
            }

            try {
                jsonObject.getString("proxy_api_prefix");
            } catch (JSONException e) {
                jsonObject.put("proxy_api_prefix", "");
                log.info("config.json没有新增proxy_api_prefix参数,现已增加！");
                exist = false;
            }

            try {
                jsonObject.getString("pool_token");
            } catch (JSONException e) {
                jsonObject.put("pool_token", "");
                log.info("config.json没有新增pool_token参数,现已增加！");
                exist = false;
            }

            config.setLoginUsername(jsonObject.getString("loginUsername"));
            config.setLoginPassword(jsonObject.getString("loginPassword"));

            config.setLicense_id(jsonObject.getString("license_id"));
            config.setAutoToken_url(jsonObject.getString("autoToken_url"));
            config.setGetTokenPassword(jsonObject.getString("getTokenPassword"));
            config.setContainerName(jsonObject.getString("containerName"));

            // 4.0
            config.setIsolated_conv_title(jsonObject.getString("isolated_conv_title"));
            config.setProxy_api_prefix(jsonObject.getString("proxy_api_prefix"));

            // 4.7
            config.setPool_token(jsonObject.getString("pool_token"));


            boolean validationExist = true;
            // 获取 captcha 相关属性
            JSONObject captchaJson = jsonObject.optJSONObject("captcha");
            if (captchaJson == null ) {
                captchaJson = new JSONObject();
            }
            validation captchaSetting = new validation(
                    captchaJson.optString("provider", ""),
                    captchaJson.optString("site_key", ""),
                    captchaJson.optString("site_secret", ""),
                    captchaJson.optBoolean("site_login", false),
                    captchaJson.optBoolean("setup_login", false),
                    captchaJson.optBoolean("oai_username", false),
                    captchaJson.optBoolean("oai_password", false)
            );
            config.setValidation(captchaSetting);

            // 获取 tls 相关属性
            boolean tlsExist = true;
            JSONObject tlsJson = jsonObject.optJSONObject("tls");
            if (tlsJson == null ) {
                tlsJson = new JSONObject();
                tlsExist = false;
            }
            tls tlsSetting = new tls(
                    tlsJson.optBoolean("enabled", false),
                    tlsJson.optString("cert_file", ""),
                    tlsJson.optString("key_file", "")
            );
            config.setTls(tlsSetting);


            if(tlsExist == false){
                jsonObject.put("tls", tlsSetting.toJSONObject());
                exist = false;
            }
            if(validationExist == false){
                jsonObject.put("captcha", captchaSetting.toJSONObject());
                exist = false;
            }
            if(exist == false){
                // 将修改后的 JSONObject 转换为格式化的 JSON 字符串
                String updatedJson = jsonObject.toString(2);
                Files.write(Paths.get(parent), updatedJson.getBytes());
            }
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String requiredPoolToken(systemSetting tem) {
        String parent = selectFile();
        try {
            // 读取 JSON 文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(parent)));

            JSONObject jsonObject = new JSONObject(jsonContent);
            updateJsonValue(jsonObject, "pool_token", tem.getPool_token());
            // 将修改后的 JSONObject 转换为格式化的 JSON 字符串
            String updatedJson = jsonObject.toString(2);
            Files.write(Paths.get(parent), updatedJson.getBytes());
            return "修改config.json成功，快去重启PandoraNext吧!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改config.json失败";
    }
}

