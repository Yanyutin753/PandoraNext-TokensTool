package com.tokensTool.pandoraNext.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokensTool.pandoraNext.chat.Conversation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Yangyang
 * @create 2023-12-25 18:29
 */

@Slf4j
@RestController()
public class chatController {
    /**
     * 是否开启tool的copilot接口
     */
    @Value("${copilot_interface}")
    private boolean copilot_interface;
    private static String machineId;

    public static void initializeMachineId() {
        try {
            machineId = generateMachineId();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        log.info("初始化machineId成功！");
    }

    private static String generateMachineId() throws NoSuchAlgorithmException {
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
    }

    /**
     * 请求体不是json 会报Request body is missing or not in JSON format
     * Authorization token缺失  会报Authorization header is missing
     * 无法请求到chat_token 会报copilot APIKey is wrong
     * @param response
     * @param request
     * @param conversation
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @PostMapping(value = "/v1/chat/completions")
    public Object conversation(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Conversation conversation) throws JSONException, IOException {
        if(conversation == null){
            return new ResponseEntity<>("Request body is missing or not in JSON format", HttpStatus.BAD_REQUEST);
        }
        String authorizationHeader = StringUtils.trimToNull(request.getHeader("Authorization"));
        String apiKey;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            apiKey = authorizationHeader.substring(7);
        } else {
            return new ResponseEntity<>("Authorization header is missing", HttpStatus.UNAUTHORIZED);
        }
        CloseableHttpClient httpClient = HttpClients.custom().build();
        // 创建HttpGet请求
        HttpGet httpGet = new HttpGet("https://api.github.com/copilot_internal/v2/token");
        httpGet.addHeader("Host", "api.github.com");
        httpGet.addHeader("authorization", "token " + apiKey);
        httpGet.addHeader("Editor-Version", "vscode/1.85.0");
        httpGet.addHeader("Editor-Plugin-Version", "copilot-chat/0.11.1");
        httpGet.addHeader("User-Agent", "GitHubCopilotChat/0.11.1");
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
        CloseableHttpResponse firstResponse = httpClient.execute(httpGet);
        String responseContent = EntityUtils.toString(firstResponse.getEntity());
        JSONObject jsonResponse = new JSONObject(responseContent);
        if (firstResponse.getStatusLine().getStatusCode() == 200 && jsonResponse.has("token")) {
            // 创建OkHttpClient请求 请求https://api.githubcopilot.com/chat/completions
            String chat_token = jsonResponse.getString("token");
            OkHttpClient client = new OkHttpClient();
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put("Host", "api.githubcopilot.com");
            headersMap.put("Accept-Encoding", "gzip, deflate, br");
            headersMap.put("Accept", "*/*");
            headersMap.put("Authorization", "Bearer " + chat_token);
            headersMap.put("X-Request-Id", UUID.randomUUID().toString());
            headersMap.put("X-Github-Api-Version", "2023-07-07");
            headersMap.put("Vscode-Sessionid", UUID.randomUUID().toString() + System.currentTimeMillis());
            headersMap.put("vscode-machineid", "your_machine_id");
            headersMap.put("Editor-Version", "vscode/1.85.0");
            headersMap.put("Editor-Plugin-Version", "copilot-chat/0.11.1");
            headersMap.put("Openai-Organization", "github-copilot");
            headersMap.put("Copilot-Integration-Id", "vscode-chat");
            headersMap.put("Openai-Intent", "conversation-panel");
            headersMap.put("User-Agent", "GitHubCopilotChat/0.11.1");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(conversation);
            // 创建一个 RequestBody 对象
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://api.githubcopilot.com/chat/completions")
                    .post(requestBody);
            headersMap.forEach(requestBuilder::addHeader);
            Request streamRequest = requestBuilder.build();
            try (Response resp = client.newCall(streamRequest).execute()) {
                if (!resp.isSuccessful()){
                    throw new IOException("Unexpected code " + resp);
                }
                response.setContentType("application/json;charset=UTF-8");
                // 输出流
                ServletOutputStream out = response.getOutputStream();
                // 输入流
                InputStream in = resp.body().byteStream();
                // 一次拿多少数据 迭代循环
                byte[] buffer = new byte[512];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            }
        }
        else {
            return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    @GetMapping("/v1/models")
    public JsonNode models() throws JsonProcessingException {
        String jsonString = "{ \"data\": [  {\"id\": \"text-search-babbage-doc-001\",\"object\": \"model\",\"created\": 1651172509,\"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"gpt-4-0613\",\"object\": \"model\",\"created\": 1686588896,\"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-4\", \"object\": \"model\", \"created\": 1687882411, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"babbage\", \"object\": \"model\", \"created\": 1649358449, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-0613\", \"object\": \"model\", \"created\": 1686587434, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-babbage-001\", \"object\": \"model\", \"created\": 1649364043, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo\", \"object\": \"model\", \"created\": 1677610602, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-1106\", \"object\": \"model\", \"created\": 1698959748, \"owned_by\": \"system\"},\n" +
                "            {\"id\": \"curie-instruct-beta\", \"object\": \"model\", \"created\": 1649364042, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-0301\", \"object\": \"model\", \"created\": 1677649963, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-16k-0613\", \"object\": \"model\", \"created\": 1685474247, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-embedding-ada-002\", \"object\": \"model\", \"created\": 1671217299, \"owned_by\": \"openai-internal\"},\n" +
                "            {\"id\": \"davinci-similarity\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"curie-similarity\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"babbage-search-document\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"curie-search-document\", \"object\": \"model\", \"created\": 1651172508, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"babbage-code-search-code\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"ada-code-search-text\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-search-curie-query-001\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-davinci-002\", \"object\": \"model\", \"created\": 1649880484, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"ada\", \"object\": \"model\", \"created\": 1649357491, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-ada-001\", \"object\": \"model\", \"created\": 1649364042, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"ada-similarity\", \"object\": \"model\", \"created\": 1651172507, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"code-search-ada-code-001\", \"object\": \"model\", \"created\": 1651172507, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-similarity-ada-001\", \"object\": \"model\", \"created\": 1651172505, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-davinci-edit-001\", \"object\": \"model\", \"created\": 1649809179, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"code-davinci-edit-001\", \"object\": \"model\", \"created\": 1649880484, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-search-curie-doc-001\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-curie-001\", \"object\": \"model\", \"created\": 1649364043, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"curie\", \"object\": \"model\", \"created\": 1649359874, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"davinci\", \"object\": \"model\", \"created\": 1649359874, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-4-0314\", \"object\": \"model\", \"created\": 1687882410, \"owned_by\": \"openai\"} ], \"object\": \"list\" }";
        return new ObjectMapper().readTree(jsonString);
    }

    /**
     * 请求体不是json 会报Request body is missing or not in JSON format
     * Authorization token缺失  会报Authorization header is missing
     * 无法请求到chat_token 会报copilot APIKey is wrong
     * @param response
     * @param request
     * @param conversation
     * @return
     * @throws JSONException
     * @throws IOException
     */
    @PostMapping(value = "/cocopilot/v1/chat/completions")
    public Object cocoPilotConversation(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Conversation conversation) throws JSONException, IOException {
        if(conversation == null){
            return new ResponseEntity<>("Request body is missing or not in JSON format", HttpStatus.BAD_REQUEST);
        }
        String authorizationHeader = StringUtils.trimToNull(request.getHeader("Authorization"));
        String apiKey;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            apiKey = authorizationHeader.substring(7);
        } else {
            return new ResponseEntity<>("Authorization header is missing", HttpStatus.UNAUTHORIZED);
        }
        CloseableHttpClient httpClient = HttpClients.custom().build();
        // 创建HttpGet请求
        HttpGet httpGet = new HttpGet("https://api.cocopilot.org/copilot_internal/v2/token");
        httpGet.addHeader("Host", "api.cocopilot.org");
        httpGet.addHeader("Authorization", "token " + apiKey);
        httpGet.addHeader("Editor-Version", "vscode/1.85.0");
        httpGet.addHeader("Editor-Plugin-Version", "copilot-chat/0.11.1");
        httpGet.addHeader("User-Agent", "GitHubCopilotChat/0.11.1");
        httpGet.addHeader("Accept", "*/*");
        CloseableHttpResponse firstResponse = httpClient.execute(httpGet);
        String responseContent = EntityUtils.toString(firstResponse.getEntity());
        JSONObject jsonResponse = new JSONObject(responseContent);
        if (firstResponse.getStatusLine().getStatusCode() == 200 && jsonResponse.has("token")) {
            // 创建OkHttpClient请求 请求https://api.githubcopilot.com/chat/completions
            String chat_token = jsonResponse.getString("token");
            OkHttpClient client = new OkHttpClient();
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put("Host", "api.githubcopilot.com");
            headersMap.put("Accept-Encoding", "gzip, deflate, br");
            headersMap.put("Accept", "*/*");
            headersMap.put("Authorization", "Bearer " + chat_token);
            headersMap.put("X-Request-Id", UUID.randomUUID().toString());
            headersMap.put("X-Github-Api-Version", "2023-07-07");
            headersMap.put("Vscode-Sessionid", UUID.randomUUID().toString() + System.currentTimeMillis());
            headersMap.put("vscode-machineid", "your_machine_id");
            headersMap.put("Editor-Version", "vscode/1.85.0");
            headersMap.put("Editor-Plugin-Version", "copilot-chat/0.11.1");
            headersMap.put("Openai-Organization", "github-copilot");
            headersMap.put("Copilot-Integration-Id", "vscode-chat");
            headersMap.put("Openai-Intent", "conversation-panel");
            headersMap.put("User-Agent", "GitHubCopilotChat/0.11.1");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(conversation);
            // 创建一个 RequestBody 对象
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://api.githubcopilot.com/chat/completions")
                    .post(requestBody);
            headersMap.forEach(requestBuilder::addHeader);
            Request streamRequest = requestBuilder.build();
            try (Response resp = client.newCall(streamRequest).execute()) {
                if (!resp.isSuccessful()){
                    throw new IOException("Unexpected code " + resp);
                }
                response.setContentType("application/json;charset=UTF-8");
                // 输出流
                ServletOutputStream out = response.getOutputStream();
                // 输入流
                InputStream in = resp.body().byteStream();
                // 一次拿多少数据 迭代循环
                byte[] buffer = new byte[512];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            }
        }
        else {
            return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    @GetMapping("/cocopilot/v1/models")
    public JsonNode cocoPilotModels() throws JsonProcessingException {
        String jsonString = "{ \"data\": [  {\"id\": \"text-search-babbage-doc-001\",\"object\": \"model\",\"created\": 1651172509,\"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"gpt-4-0613\",\"object\": \"model\",\"created\": 1686588896,\"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-4\", \"object\": \"model\", \"created\": 1687882411, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"babbage\", \"object\": \"model\", \"created\": 1649358449, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-0613\", \"object\": \"model\", \"created\": 1686587434, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-babbage-001\", \"object\": \"model\", \"created\": 1649364043, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo\", \"object\": \"model\", \"created\": 1677610602, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-1106\", \"object\": \"model\", \"created\": 1698959748, \"owned_by\": \"system\"},\n" +
                "            {\"id\": \"curie-instruct-beta\", \"object\": \"model\", \"created\": 1649364042, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-0301\", \"object\": \"model\", \"created\": 1677649963, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-3.5-turbo-16k-0613\", \"object\": \"model\", \"created\": 1685474247, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-embedding-ada-002\", \"object\": \"model\", \"created\": 1671217299, \"owned_by\": \"openai-internal\"},\n" +
                "            {\"id\": \"davinci-similarity\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"curie-similarity\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"babbage-search-document\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"curie-search-document\", \"object\": \"model\", \"created\": 1651172508, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"babbage-code-search-code\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"ada-code-search-text\", \"object\": \"model\", \"created\": 1651172510, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-search-curie-query-001\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-davinci-002\", \"object\": \"model\", \"created\": 1649880484, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"ada\", \"object\": \"model\", \"created\": 1649357491, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-ada-001\", \"object\": \"model\", \"created\": 1649364042, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"ada-similarity\", \"object\": \"model\", \"created\": 1651172507, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"code-search-ada-code-001\", \"object\": \"model\", \"created\": 1651172507, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-similarity-ada-001\", \"object\": \"model\", \"created\": 1651172505, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-davinci-edit-001\", \"object\": \"model\", \"created\": 1649809179, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"code-davinci-edit-001\", \"object\": \"model\", \"created\": 1649880484, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"text-search-curie-doc-001\", \"object\": \"model\", \"created\": 1651172509, \"owned_by\": \"openai-dev\"},\n" +
                "            {\"id\": \"text-curie-001\", \"object\": \"model\", \"created\": 1649364043, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"curie\", \"object\": \"model\", \"created\": 1649359874, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"davinci\", \"object\": \"model\", \"created\": 1649359874, \"owned_by\": \"openai\"},\n" +
                "            {\"id\": \"gpt-4-0314\", \"object\": \"model\", \"created\": 1687882410, \"owned_by\": \"openai\"} ], \"object\": \"list\" }";
        return new ObjectMapper().readTree(jsonString);
    }
}
