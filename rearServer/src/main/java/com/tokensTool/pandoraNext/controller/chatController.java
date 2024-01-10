package com.tokensTool.pandoraNext.controller;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokensTool.pandoraNext.chat.Conversation;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.modelsUsage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Yangyang
 * @create 2023-12-25 18:29
 */

@Slf4j
@RestController()
public class chatController {

    /**
     * 缓存cocopilotToken
     */
    private static final HashMap<String, String> copilotTokenList;
    /**
     * 缓存copilotToken
     */
    private static final HashMap<String, String> coCopilotTokenList;
    /**
     * 模型
     */
    private static final String models = "{\"data\":[{\"id\":\"text-search-babbage-doc-001\",\"object\":\"model\",\"created\":1651172509,\"owned_by\":\"openai-dev\"},{\"id\":\"gpt-4\",\"object\":\"model\",\"created\":1687882411,\"owned_by\":\"openai\"},{\"id\":\"babbage\",\"object\":\"model\",\"created\":1649358449,\"owned_by\":\"openai\"},{\"id\":\"gpt-3.5-turbo-0613\",\"object\":\"model\",\"created\":1686587434,\"owned_by\":\"openai\"},{\"id\":\"text-babbage-001\",\"object\":\"model\",\"created\":1649364043,\"owned_by\":\"openai\"},{\"id\":\"gpt-3.5-turbo\",\"object\":\"model\",\"created\":1677610602,\"owned_by\":\"openai\"},{\"id\":\"gpt-3.5-turbo-1106\",\"object\":\"model\",\"created\":1698959748,\"owned_by\":\"system\"},{\"id\":\"curie-instruct-beta\",\"object\":\"model\",\"created\":1649364042,\"owned_by\":\"openai\"},{\"id\":\"gpt-3.5-turbo-0301\",\"object\":\"model\",\"created\":1677649963,\"owned_by\":\"openai\"},{\"id\":\"gpt-3.5-turbo-16k-0613\",\"object\":\"model\",\"created\":1685474247,\"owned_by\":\"openai\"},{\"id\":\"text-embedding-ada-002\",\"object\":\"model\",\"created\":1671217299,\"owned_by\":\"openai-internal\"},{\"id\":\"davinci-similarity\",\"object\":\"model\",\"created\":1651172509,\"owned_by\":\"openai-dev\"},{\"id\":\"curie-similarity\",\"object\":\"model\",\"created\":1651172510,\"owned_by\":\"openai-dev\"},{\"id\":\"babbage-search-document\",\"object\":\"model\",\"created\":1651172510,\"owned_by\":\"openai-dev\"},{\"id\":\"curie-search-document\",\"object\":\"model\",\"created\":1651172508,\"owned_by\":\"openai-dev\"},{\"id\":\"babbage-code-search-code\",\"object\":\"model\",\"created\":1651172509,\"owned_by\":\"openai-dev\"},{\"id\":\"ada-code-search-text\",\"object\":\"model\",\"created\":1651172510,\"owned_by\":\"openai-dev\"},{\"id\":\"text-search-curie-query-001\",\"object\":\"model\",\"created\":1651172509,\"owned_by\":\"openai-dev\"},{\"id\":\"text-davinci-002\",\"object\":\"model\",\"created\":1649880484,\"owned_by\":\"openai\"},{\"id\":\"ada\",\"object\":\"model\",\"created\":1649357491,\"owned_by\":\"openai\"},{\"id\":\"text-ada-001\",\"object\":\"model\",\"created\":1649364042,\"owned_by\":\"openai\"},{\"id\":\"ada-similarity\",\"object\":\"model\",\"created\":1651172507,\"owned_by\":\"openai-dev\"},{\"id\":\"code-search-ada-code-001\",\"object\":\"model\",\"created\":1651172507,\"owned_by\":\"openai-dev\"},{\"id\":\"text-similarity-ada-001\",\"object\":\"model\",\"created\":1651172505,\"owned_by\":\"openai-dev\"},{\"id\":\"text-davinci-edit-001\",\"object\":\"model\",\"created\":1649809179,\"owned_by\":\"openai\"},{\"id\":\"code-davinci-edit-001\",\"object\":\"model\",\"created\":1649880484,\"owned_by\":\"openai\"},{\"id\":\"text-search-curie-doc-001\",\"object\":\"model\",\"created\":1651172509,\"owned_by\":\"openai-dev\"},{\"id\":\"text-curie-001\",\"object\":\"model\",\"created\":1649364043,\"owned_by\":\"openai\"},{\"id\":\"curie\",\"object\":\"model\",\"created\":1649359874,\"owned_by\":\"openai\"},{\"id\":\"davinci\",\"object\":\"model\",\"created\":1649359874,\"owned_by\":\"openai\"}]}";
    private static final String machineId;
    private static HashMap<String, Integer> modelsUsage;

    static {
        modelsUsage = new HashMap<>();
        modelsUsage.put("gpt-4",0);
        modelsUsage.put("gpt-3.5-turbo",0);
        modelsUsage.put("gpt-3.5-turbo-0301",0);
        log.info("初始化ipList成功！");
    }

    static {
        copilotTokenList = new HashMap<>();
        coCopilotTokenList = new HashMap<>();
        machineId = generateMachineId();
        log.info("初始化chat接口需求成功！");
    }

    @Value("${copilot_interface}")
    private boolean copilot_interface;

    private static String generateMachineId() {
        try {
            UUID uuid = UUID.randomUUID();
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(uuid.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    private void clearModelsUsage() {
        HashMap<String, Integer> newModelsUsaget = new HashMap<>();
        newModelsUsaget.put("gpt-4",0);
        newModelsUsaget.put("gpt-3.5-turbo",0);
        newModelsUsaget.put("gpt-3.5-turbo-0301",0);
        modelsUsage = newModelsUsaget;
        log.info("重置modelsUsage成功！");
    }

    /**
     * 请求体不是json 会报Request body is missing or not in JSON format
     * Authorization token缺失  会报Authorization header is missing
     * 无法请求到chat_token 会报copilot APIKey is wrong
     *
     * @param response
     * @param request
     * @param conversation
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @PostMapping(value = "/v1/chat/completions")
    public Object coPilotConversation(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Conversation conversation) {
        try {
            if (conversation == null) {
                return new ResponseEntity<>("Request body is missing or not in JSON format", HttpStatus.BAD_REQUEST);
            }
            String authorizationHeader = StringUtils.trimToNull(request.getHeader("Authorization"));
            String apiKey;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                apiKey = authorizationHeader.substring(7);
            } else {
                return new ResponseEntity<>("Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }
            if (!copilotTokenList.containsKey(apiKey)) {
                String token = getCopilotToken(apiKey);
                if (token == null) {
                    return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                }
                copilotTokenList.put(apiKey, token);
                log.info("coCopilotTokenList初始化成功！");
            }
            // 创建OkHttpClient请求 请求https://api.githubcopilot.com/chat/completions
            String chat_token = copilotTokenList.get(apiKey);
            OkHttpClient client = productClient(5);
            Map<String, String> headersMap = new HashMap<>();
            //添加头部
            addHeader(headersMap, chat_token);
            String json = JSON.toJSONString(conversation);
            // 创建一个 RequestBody 对象
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://api.githubcopilot.com/chat/completions")
                    .post(requestBody);
            headersMap.forEach(requestBuilder::addHeader);
            Request streamRequest = requestBuilder.build();
            try (Response resp = client.newCall(streamRequest).execute()) {
                if (!resp.isSuccessful()) {
                    String token = getCopilotToken(apiKey);
                    if (token == null) {
                        return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                    }
                    copilotTokenList.put(apiKey, token);
                    log.info("coCopilotTokenList重置化成功！");
                    coPilotConversation(response, request, conversation);
                    return null;
                }
                // 流式和非流式输出
                outPutChat(response, resp, conversation);
                addModel(conversation);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 请求体不是json 会报Request body is missing or not in JSON format
     * Authorization token缺失  会报Authorization header is missing
     * 无法请求到chat_token 会报copilot APIKey is wrong
     *
     * @param response
     * @param request
     * @param conversation
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @PostMapping(value = "/cocopilot/v1/chat/completions")
    public Object coCoPilotConversation(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Conversation conversation) {
        try {
            if (conversation == null) {
                return new ResponseEntity<>("Request body is missing or not in JSON format", HttpStatus.BAD_REQUEST);
            }
            String authorizationHeader = StringUtils.trimToNull(request.getHeader("Authorization"));
            String apiKey;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                apiKey = authorizationHeader.substring(7);
            } else {
                return new ResponseEntity<>("Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }
            if (!coCopilotTokenList.containsKey(apiKey)) {
                String token = getCoCoToken(apiKey);
                if (token == null) {
                    return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                }
                coCopilotTokenList.put(apiKey, token);
                log.info("coCopilotTokenList初始化成功！");
            }
            // 创建OkHttpClient请求 请求https://api.githubcopilot.com/chat/completions
            String chat_token = coCopilotTokenList.get(apiKey);
            OkHttpClient client = productClient(5);
            Map<String, String> headersMap = new HashMap<>();
            //添加头部
            addHeader(headersMap, chat_token);
            String json = JSON.toJSONString(conversation);
            // 创建一个 RequestBody 对象
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://api.githubcopilot.com/chat/completions")
                    .post(requestBody);
            headersMap.forEach(requestBuilder::addHeader);
            Request streamRequest = requestBuilder.build();
            try (Response resp = client.newCall(streamRequest).execute()) {
                if (!resp.isSuccessful()) {
                    String token = getCoCoToken(apiKey);
                    if (token == null) {
                        return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                    }
                    coCopilotTokenList.put(apiKey, token);
                    log.info("coCopilotTokenList重置化成功！");
                    coCoPilotConversation(response, request, conversation);
                    return null;
                }
                // 流式和非流式输出
                outPutChat(response, resp, conversation);
                addModel(conversation);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addModel(Conversation conversation) {
        String model = conversation.getModel();
        if (modelsUsage.containsKey(model)) {
            modelsUsage.put(model, modelsUsage.get(model) + 1);
        } else {
            modelsUsage.put(model, 1);
        }
    }

    @GetMapping(value = "api/modelsUsage")
    private Result getModelUsage() {
        try {
            List<modelsUsage> res = new ArrayList();
            modelsUsage.forEach((key, value) -> res.add(new modelsUsage(key, value)));
            return Result.success(res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getCopilotToken(String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/copilot_internal/v2/token")
                .addHeader("Host", "api.github.com")
                .addHeader("authorization", "token " + apiKey)
                .addHeader("Editor-Version", "vscode/1.85.0")
                .addHeader("Editor-Plugin-Version", "copilot-chat/0.11.1")
                .addHeader("User-Agent", "GitHubCopilotChat/0.11.1")
                .addHeader("Accept", "*/*")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.has("token") ? jsonResponse.get("token").toString() : null;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCoCoToken(String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.cocopilot.org/copilot_internal/v2/token")
                .addHeader("Host", "api.cocopilot.org")
                .addHeader("authorization", "token " + apiKey)
                .addHeader("Editor-Version", "vscode/1.85.0")
                .addHeader("Editor-Plugin-Version", "copilot-chat/0.11.1")
                .addHeader("User-Agent", "GitHubCopilotChat/0.11.1")
                .addHeader("Accept", "*/*")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.has("token") ? jsonResponse.get("token").toString() : null;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/v1/models")
    public JsonNode models() throws JsonProcessingException {
        String jsonString = models;
        return new ObjectMapper().readTree(jsonString);
    }

    @GetMapping("/cocopilot/v1/models")
    public JsonNode cocoPilotModels() throws JsonProcessingException {
        String jsonString = models;
        return new ObjectMapper().readTree(jsonString);
    }

    private void addHeader(Map<String, String> headersMap, String chat_token) {
        headersMap.put("Host", "api.githubcopilot.com");
        headersMap.put("Accept-Encoding", "gzip, deflate, br");
        headersMap.put("Accept", "*/*");
        headersMap.put("Authorization", "Bearer " + chat_token);
        headersMap.put("X-Request-Id", UUID.randomUUID().toString());
        headersMap.put("X-Github-Api-Version", "2023-07-07");
        headersMap.put("Vscode-Sessionid", UUID.randomUUID().toString() + System.currentTimeMillis());
        headersMap.put("vscode-machineid", machineId);
        headersMap.put("Editor-Version", "vscode/1.85.0");
        headersMap.put("Editor-Plugin-Version", "copilot-chat/0.11.1");
        headersMap.put("Openai-Organization", "github-copilot");
        headersMap.put("Copilot-Integration-Id", "vscode-chat");
        headersMap.put("Openai-Intent", "conversation-panel");
        headersMap.put("User-Agent", "GitHubCopilotChat/0.11.1");
    }


    private void outPutChat(HttpServletResponse response, Response resp, Conversation conversation) {
        try {
            Boolean isStream = conversation.getStream();
            if (isStream != null && isStream) {
                response.setContentType("text/event-stream; charset=UTF-8");
            } else {
                response.setContentType("application/json; charset=utf-8");
            }
            // 输出流
            ServletOutputStream out = response.getOutputStream();
            // 输入流
            InputStream in = resp.body().byteStream();
            // 一次拿多少数据 迭代循环
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OkHttpClient productClient(Integer timeout) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(timeout, TimeUnit.MINUTES);
            builder.readTimeout(timeout, TimeUnit.MINUTES);
            builder.writeTimeout(timeout, TimeUnit.MINUTES);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
