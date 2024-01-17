package com.tokensTool.pandoraNext.controller;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokensTool.pandoraNext.chat.Conversation;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.modelsUsage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

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
        modelsUsage.put("gpt-4", 0);
        modelsUsage.put("gpt-3.5-turbo", 0);
        modelsUsage.put("text-embedding-ada-002", 0);
        copilotTokenList = new HashMap<>();
        coCopilotTokenList = new HashMap<>();
        machineId = generateMachineId();
        log.info("初始化chat接口需求成功！");
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build();
    private final ExecutorService executor = new ThreadPoolExecutor(0, 300,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());
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
        newModelsUsaget.put("gpt-4", 0);
        newModelsUsaget.put("gpt-3.5-turbo", 0);
        newModelsUsaget.put("gpt-3.5-turbo-0301", 0);
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
    public CompletableFuture<ResponseEntity<String>> coPilotConversation(HttpServletResponse response, HttpServletRequest request,
                                                                         @org.springframework.web.bind.annotation.RequestBody Conversation conversation) {
        return CompletableFuture.supplyAsync(() -> {
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
                        Map<String, String> headersMap = new HashMap<>();
                        //添加头部
                        addHeader(headersMap, chat_token);
                        String json = com.alibaba.fastjson2.JSON.toJSONString(conversation);
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
                                if (resp.code() == 429) {
                                    return new ResponseEntity<>("rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                                } else {
                                    String token = getCopilotToken(apiKey);
                                    if (token == null) {
                                        return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                                    }
                                    copilotTokenList.put(apiKey, token);
                                    log.info("token过期，coCopilotTokenList重置化成功！");
                                    againConversation(response, conversation, token);
                                }
                            } else {
                                // 流式和非流式输出
                                outPutChat(response, resp, conversation);
                                addModel(conversation);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }, executor)
                .orTimeout(6, TimeUnit.MINUTES)
                .exceptionally(ex -> {
                    // 处理超时或其他异常
                    if (ex instanceof TimeoutException) {
                        return new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT);
                    } else {
                        return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
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
    public CompletableFuture<ResponseEntity<String>> coCoPilotConversation(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Conversation conversation) throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
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
                                return new ResponseEntity<>("cocopilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                            }
                            coCopilotTokenList.put(apiKey, token);
                            log.info("coCopilotTokenList初始化成功！");
                        }
                        // 创建OkHttpClient请求 请求https://api.githubcopilot.com/chat/completions
                        String chat_token = coCopilotTokenList.get(apiKey);
                        Map<String, String> headersMap = new HashMap<>();
                        //添加头部
                        addHeader(headersMap, chat_token);
                        String json = com.alibaba.fastjson2.JSON.toJSONString(conversation);
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
                                if (resp.code() == 429) {
                                    return new ResponseEntity<>("rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                                } else {
                                    String token = getCoCoToken(apiKey);
                                    if (token == null) {
                                        return new ResponseEntity<>("cocopilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                                    }
                                    coCopilotTokenList.put(apiKey, token);
                                    log.info("token过期，coCopilotTokenList重置化成功！");
                                    againConversation(response, conversation, token);
                                }
                            } else {
                                // 流式和非流式输出
                                outPutChat(response, resp, conversation);
                                addModel(conversation);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }, executor)
                .orTimeout(6, TimeUnit.MINUTES)
                .exceptionally(ex -> {
                    // 处理超时或其他异常
                    if (ex instanceof TimeoutException) {
                        return new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT);
                    } else {
                        return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }


    /**
     * 如发现token过期
     * 重新回复问题
     *
     * @param response
     * @param conversation
     * @param token
     * @return
     */
    public Object againConversation(HttpServletResponse response,
                                    @org.springframework.web.bind.annotation.RequestBody Conversation conversation,
                                    String token) {
        try {
            Map<String, String> headersMap = new HashMap<>();
            //添加头部
            addHeader(headersMap, token);
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
                    return new ResponseEntity<>("copilot/cocopilot APIKey is wrong Or your network is wrong", HttpStatus.UNAUTHORIZED);
                } else {
                    // 流式和非流式输出
                    outPutChat(response, resp, conversation);
                    addModel(conversation);
                }
            }
            return null;
        } catch (Exception e) {
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
    @PostMapping(value = "/v1/embeddings")
    public Object coPilotEmbeddings(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Object conversation) {
        return CompletableFuture.supplyAsync(() -> {
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
                        Map<String, String> headersMap = new HashMap<>();
                        //添加头部
                        addHeader(headersMap, chat_token);
                        String json = com.alibaba.fastjson2.JSON.toJSONString(conversation);
                        // 创建一个 RequestBody 对象
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(json, JSON);
                        Request.Builder requestBuilder = new Request.Builder()
                                .url("https://api.githubcopilot.com/embeddings")
                                .post(requestBody);
                        headersMap.forEach(requestBuilder::addHeader);
                        Request streamRequest = requestBuilder.build();
                        try (Response resp = client.newCall(streamRequest).execute()) {
                            if (!resp.isSuccessful()) {
                                if (resp.code() == 429) {
                                    return new ResponseEntity<>("rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                                } else {
                                    String token = getCopilotToken(apiKey);
                                    if (token == null) {
                                        return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                                    }
                                    copilotTokenList.put(apiKey, token);
                                    log.info("token过期，coCopilotTokenList重置化成功！");
                                    againEmbeddings(response, conversation, token);
                                }
                            } else {
                                // 非流式输出
                                outPutEmbeddings(response, resp);
                                com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(json);
                                String model = jsonObject.getString("model");
                                if (modelsUsage.containsKey(model)) {
                                    modelsUsage.put(model, modelsUsage.get(model) + 1);
                                } else {
                                    modelsUsage.put(model, 1);
                                }
                            }
                        }
                        return null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
                .orTimeout(6, TimeUnit.MINUTES)
                .exceptionally(ex -> {
                    // 处理超时或其他异常
                    if (ex instanceof TimeoutException) {
                        return new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT);
                    } else {
                        return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }


    @PostMapping(value = "/cocopilot/v1/embeddings")
    public Object coCoPilotEmbeddings(HttpServletResponse response, HttpServletRequest request, @org.springframework.web.bind.annotation.RequestBody Object conversation) {
        return CompletableFuture.supplyAsync(() -> {
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
                        Map<String, String> headersMap = new HashMap<>();
                        //添加头部
                        addHeader(headersMap, chat_token);
                        String json = com.alibaba.fastjson2.JSON.toJSONString(conversation);
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(json, JSON);
                        Request.Builder requestBuilder = new Request.Builder()
                                .url("https://api.githubcopilot.com/embeddings")
                                .post(requestBody);
                        headersMap.forEach(requestBuilder::addHeader);
                        Request streamRequest = requestBuilder.build();
                        try (Response resp = client.newCall(streamRequest).execute()) {
                            if (!resp.isSuccessful()) {
                                if (resp.code() == 429) {
                                    return new ResponseEntity<>("rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                                } else {
                                    String token = getCoCoToken(apiKey);
                                    if (token == null) {
                                        return new ResponseEntity<>("copilot APIKey is wrong", HttpStatus.UNAUTHORIZED);
                                    }
                                    coCopilotTokenList.put(apiKey, token);
                                    log.info("token过期，coCopilotTokenList重置化成功！");
                                    againEmbeddings(response, conversation, token);
                                }
                            } else {
                                // 非流式输出
                                outPutEmbeddings(response, resp);
                                com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(json);
                                String model = jsonObject.getString("model");
                                if (modelsUsage.containsKey(model)) {
                                    modelsUsage.put(model, modelsUsage.get(model) + 1);
                                } else {
                                    modelsUsage.put(model, 1);
                                }
                            }
                        }
                        return null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
                .orTimeout(6, TimeUnit.MINUTES)
                .exceptionally(ex -> {
                    // 处理超时或其他异常
                    if (ex instanceof TimeoutException) {
                        return new ResponseEntity<>("Request timed out", HttpStatus.REQUEST_TIMEOUT);
                    } else {
                        return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    public Object againEmbeddings(HttpServletResponse response,
                                  @org.springframework.web.bind.annotation.RequestBody Object conversation,
                                  String token) {
        try {
            Map<String, String> headersMap = new HashMap<>();
            //添加头部
            addHeader(headersMap, token);
            String json = JSON.toJSONString(conversation);
            // 创建一个 RequestBody 对象
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, JSON);
            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://api.githubcopilot.com/embeddings")
                    .post(requestBody);
            headersMap.forEach(requestBuilder::addHeader);
            Request streamRequest = requestBuilder.build();
            try (Response resp = client.newCall(streamRequest).execute()) {
                if (!resp.isSuccessful()) {
                    return new ResponseEntity<>("copilot/cocopilot APIKey is wrong Or your network is wrong", HttpStatus.UNAUTHORIZED);
                } else {
                    // 非流式输出
                    outPutEmbeddings(response, resp);
                    com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(json);
                    String model = jsonObject.getString("model");
                    if (modelsUsage.containsKey(model)) {
                        modelsUsage.put(model, modelsUsage.get(model) + 1);
                    } else {
                        modelsUsage.put(model, 1);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加模型，用于监控
     *
     * @param conversation
     */
    private void addModel(Conversation conversation) {
        String model = conversation.getModel();
        if (modelsUsage.containsKey(model)) {
            modelsUsage.put(model, modelsUsage.get(model) + 1);
        } else {
            modelsUsage.put(model, 1);
        }
    }

    /**
     * 获取用量
     *
     * @return
     */
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

    /**
     * 用于copilot——ghu或gho 拿到token
     *
     * @param apiKey
     * @return
     * @throws IOException
     */
    private String getCopilotToken(String apiKey) throws IOException {
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

    /**
     * 用于cocopilot——ccu 拿到token
     *
     * @param apiKey
     * @return
     * @throws IOException
     */
    private String getCoCoToken(String apiKey) throws IOException {
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


    /**
     * copilot的模型
     *
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/v1/models")
    public Object models() throws JsonProcessingException {
        try {
            Future<Object> future = executor.submit(() -> {
                String jsonString = models;
                return new ObjectMapper().readTree(jsonString);
            });
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * cocopilot的模型
     *
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/cocopilot/v1/models")
    public Object cocoPilotModels() {
        try {
            Future<Object> future = executor.submit(() -> {
                String jsonString = models;
                return new ObjectMapper().readTree(jsonString);
            });
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 提问请求头
     *
     * @param headersMap
     * @param chat_token
     */
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

    /**
     * chat接口的输出
     *
     * @param response
     * @param resp
     * @param conversation
     */
    private void outPutChat(HttpServletResponse response, Response resp, Conversation conversation) {
        try {
            Boolean isStream = conversation.getStream();
            int one_messageByte;
            int sleep_time;
            if (isStream != null && isStream) {
                if (!conversation.getModel().contains("gpt-4")) {
                    one_messageByte = 2048;
                    sleep_time = 0;
                } else {
                    one_messageByte = 128;
                    sleep_time = 24;
                }
                response.setContentType("text/event-stream; charset=UTF-8");
            } else {
                one_messageByte = 8192;
                sleep_time = 0;
                response.setContentType("application/json; charset=utf-8");
            }
            OutputStream out = new BufferedOutputStream(response.getOutputStream());
            InputStream in = new BufferedInputStream(resp.body().byteStream());
            // 一次拿多少数据 迭代循环
            byte[] buffer = new byte[one_messageByte];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
                try {
                    Thread.sleep(sleep_time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Embeddings接口的输出
     *
     * @param response
     * @param resp
     */
    private void outPutEmbeddings(HttpServletResponse response, Response resp) {
        try {
            response.setContentType("application/json; charset=utf-8");
            OutputStream out = new BufferedOutputStream(response.getOutputStream());
            InputStream in = new BufferedInputStream(resp.body().byteStream());
            // 一次拿多少数据 迭代循环
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
