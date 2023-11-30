package com.yyandywt99.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Yangyang
 * @create 2023-11-29 12:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class tls {

    /**
     * 是否开启证书
     */
    private boolean enabled;

    /**
     * 是否证书文件
     */
    private String cert_file;

    /**
     * 是否密钥文件
     */
    private String key_file;

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("enabled", this.enabled);
        json.put("cert_file", this.cert_file);
        json.put("key_file", this.key_file);
        return json;
    }
}
