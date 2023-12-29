package com.tokensTool.pandoraNext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class TokensToolApplicationTests {

//    @Test
//    public void test1(){
//        OkHttpClient httpClient = new OkHttpClient();
//        String url = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + "117.136.12.172" +"&amp;json=true";
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//        try {
//            Response response = httpClient.newCall(request).execute();
//            String result = response.body().string();
//            int startIndex = result.indexOf("{");
//            int endIndex = result.lastIndexOf("}");
//            if (startIndex != -1 && endIndex != -1) {
//                // Find the actual start and end of the JSON data
//                int jsonStartIndex = result.indexOf("{", startIndex + 1);
//                int jsonEndIndex = result.lastIndexOf("}", endIndex - 1);
//                if (jsonStartIndex != -1 && jsonEndIndex != -1) {
//                    String json = result.substring(jsonStartIndex, jsonEndIndex + 1);
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    Map resultMap = objectMapper.readValue(json, Map.class);
//                    System.out.println("ip信息：" + resultMap);
//
//                    String pro = (String) resultMap.get("addr");
//                    String city = (String) resultMap.get("city");
//                    System.out.println(pro + city);
//                } else {
//                    System.out.println("No JSON object found in the response");
//                }
//            } else {
//                System.out.println("No JSON object found in the response");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
