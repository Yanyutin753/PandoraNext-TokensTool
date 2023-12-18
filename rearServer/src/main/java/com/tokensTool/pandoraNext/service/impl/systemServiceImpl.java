package com.tokensTool.pandoraNext.service.impl;

import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.pojo.tls;
import com.tokensTool.pandoraNext.pojo.validation;
import com.tokensTool.pandoraNext.service.systemService;
import com.tokensTool.pandoraNext.util.JwtUtils;
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
import java.util.*;

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
     * 初始化config.json文件
     *
     */
    public void initializeConfigJson() {
        String parent = selectFile();
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(parent)));
            JSONObject jsonObject = new JSONObject(jsonContent);

            // 定义一个 Map，其中键是 JSON 属性的名称，值是默认值
            Map<String, Object> keysAndDefaults = new HashMap<>();
            keysAndDefaults.put("loginUsername", "root");
            keysAndDefaults.put("loginPassword", "123456");
            keysAndDefaults.put("license_id", "");
            keysAndDefaults.put("autoToken_url", "default");
            keysAndDefaults.put("isGetToken", "false");
            keysAndDefaults.put("getTokenPassword", "");
            keysAndDefaults.put("containerName", "PandoraNext");
            keysAndDefaults.put("isolated_conv_title", "*");
            keysAndDefaults.put("proxy_api_prefix", "");
            keysAndDefaults.put("disable_signup", false);
            keysAndDefaults.put("auto_conv_arkose", false);
            keysAndDefaults.put("proxy_file_service", false);
            keysAndDefaults.put("custom_doh_host", "");

            // 0.4.9.2
            keysAndDefaults.put("auto_updateSession",false);
            keysAndDefaults.put("auto_updateTime",5);
            keysAndDefaults.put("auto_updateNumber",1);
            keysAndDefaults.put("pandoraNext_outUrl","");

            boolean exist = checkAndSetDefaults(jsonObject, keysAndDefaults);

            JSONObject captchaJson = Optional.ofNullable(jsonObject.optJSONObject("captcha")).orElse(new JSONObject());
            validation captchaSetting = new validation(
                    captchaJson.optString("provider", ""),
                    captchaJson.optString("site_key", ""),
                    captchaJson.optString("site_secret", ""),
                    captchaJson.optBoolean("site_login", false),
                    captchaJson.optBoolean("setup_login", false),
                    captchaJson.optBoolean("oai_username", false),
                    captchaJson.optBoolean("oai_password", false)
            );

            JSONObject tlsJson = Optional.ofNullable(jsonObject.optJSONObject("tls")).orElse(new JSONObject());
            tls tlsSetting = new tls(
                    tlsJson.optBoolean("enabled", false),
                    tlsJson.optString("cert_file", ""),
                    tlsJson.optString("key_file", "")
            );

            if (tlsJson.length() == 0) {
                jsonObject.put("tls", tlsSetting.toJSONObject());
                exist = false;
            }
            if (captchaJson.length() == 0) {
                jsonObject.put("captcha", captchaSetting.toJSONObject());
                exist = false;
            }
            if (!exist) {
                String updatedJson = jsonObject.toString(2);
                Files.write(Paths.get(parent), updatedJson.getBytes());
            }
            log.info("初始化config.json成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 通用方法，检查和设置默认值
    private boolean checkAndSetDefaults(JSONObject jsonObject, Map<String, Object> keysAndDefaults) throws JSONException {
        boolean exist = true;
        for (Map.Entry<String, Object> entry : keysAndDefaults.entrySet()) {
            try {
                jsonObject.get(entry.getKey());
            } catch (JSONException e) {
                jsonObject.put(entry.getKey(), entry.getValue());
                log.info("config.json没有新增" + entry.getKey() + "参数,现已增加！");
                exist = false;
            }
        }
        return exist;
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
            updateJsonValue(jsonObject,"isGetToken",tem.getIsGetToken());
            updateJsonValue(jsonObject,"getTokenPassword",tem.getGetTokenPassword());
            updateJsonValue(jsonObject,"containerName",tem.getContainerName());

            updateJsonValue(jsonObject,"isolated_conv_title",tem.getIsolated_conv_title());
            updateJsonValue(jsonObject,"proxy_api_prefix",tem.getProxy_api_prefix());

            // 4,9
            updateJsonValue(jsonObject,"disable_signup",tem.getDisable_signup());
            updateJsonValue(jsonObject,"auto_conv_arkose",tem.getAuto_conv_arkose());
            updateJsonValue(jsonObject,"proxy_file_service",tem.getProxy_file_service());
            updateJsonValue(jsonObject,"custom_doh_host",tem.getCustom_doh_host());

            // 0.4.9.2
            updateJsonValue(jsonObject,"auto_updateSession",tem.getAuto_updateSession());
            updateJsonValue(jsonObject,"auto_updateTime",tem.getAuto_updateTime());
            updateJsonValue(jsonObject,"auto_updateNumber",tem.getAuto_updateNumber());
            updateJsonValue(jsonObject,"pandoraNext_outUrl",tem.getPandoraNext_outUrl());

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
        if(value == null) {
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
    public systemSetting selectSetting() {
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
            config.setTimeout(jsonObject.optInt("timeout"));

            config.setLoginUsername(jsonObject.optString("loginUsername"));
            config.setLoginPassword(jsonObject.optString("loginPassword"));

            config.setLicense_id(jsonObject.optString("license_id"));
            config.setAutoToken_url(jsonObject.optString("autoToken_url"));
            config.setIsGetToken(jsonObject.optBoolean("isGetToken"));
            config.setGetTokenPassword(jsonObject.optString("getTokenPassword"));
            config.setContainerName(jsonObject.optString("containerName"));

            // 4.0
            config.setIsolated_conv_title(jsonObject.optString("isolated_conv_title"));
            config.setProxy_api_prefix(jsonObject.optString("proxy_api_prefix"));

            // 4.9
            config.setDisable_signup(jsonObject.optBoolean("disable_signup"));
            config.setAuto_conv_arkose(jsonObject.optBoolean("auto_conv_arkose"));
            config.setProxy_file_service(jsonObject.optBoolean("proxy_file_service"));
            config.setCustom_doh_host(jsonObject.optString("custom_doh_host"));

            // 0.4.9.2
            config.setAuto_updateSession(jsonObject.optBoolean("auto_updateSession"));
            config.setAuto_updateTime(jsonObject.optInt("auto_updateTime"));
            config.setAuto_updateNumber(jsonObject.optInt("auto_updateNumber"));
            config.setPandoraNext_outUrl(jsonObject.optString("pandoraNext_outUrl"));

            // 获取 captcha 相关属性
            JSONObject captchaJson = Optional.ofNullable(jsonObject.optJSONObject("captcha")).orElse(new JSONObject());
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
            JSONObject tlsJson = Optional.ofNullable(jsonObject.optJSONObject("tls")).orElse(new JSONObject());
            tls tlsSetting = new tls(
                    tlsJson.optBoolean("enabled", false),
                    tlsJson.optString("cert_file", ""),
                    tlsJson.optString("key_file", "")
            );
            config.setTls(tlsSetting);
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String requireTimeTask(systemSetting tem){
        String parent = selectFile();
        try {
            // 读取 JSON 文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get(parent)));

            JSONObject jsonObject = new JSONObject(jsonContent);
            // 0.4.9.2
            updateJsonValue(jsonObject,"auto_updateSession",tem.getAuto_updateSession());
            updateJsonValue(jsonObject,"auto_updateTime",tem.getAuto_updateTime());
            updateJsonValue(jsonObject,"auto_updateNumber",tem.getAuto_updateNumber());
            updateJsonValue(jsonObject,"pandoraNext_outUrl",tem.getPandoraNext_outUrl());
            // 将修改后的 JSONObject 转换为格式化的 JSON 字符串
            String updatedJson = jsonObject.toString(2);
            Files.write(Paths.get(parent), updatedJson.getBytes());
            return "修改定时任务和url成功！";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "修改定时任务和url失败！";
    }

}

