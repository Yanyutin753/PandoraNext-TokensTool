package com.tokensTool.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    /**
     * 响应码，1 代表成功; 0 代表失败
     */

    private Integer code;
    /**
     * 响应信息 描述字符串
     */

    private String msg;
    /**
     * 返回的数据
     */
    private Object data;

    /**
     * 增删改 成功响应
     */
    public static Result success() {
        return new Result(1, "success", null);
    }

    /**
     * 查询 成功响应
     */
    public static Result success(Object data) {
        return new Result(1, "success", data);
    }

    /**
     * 失败响应
     */
    public static Result error(String msg) {
        return new Result(0, msg, null);
    }
}