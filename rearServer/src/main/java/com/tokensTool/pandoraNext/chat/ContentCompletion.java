package com.tokensTool.pandoraNext.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentCompletion {

    /**
     * 指定要使用的模型
     */
    private String model;


    /**
     * 提供一个文本片段，作为模型生成文本的启动点或上下文
     */
    private String prompt;


    /**
     * 限制生成文本的最大长度，以一定数量的 tokens（标记）为单位。
     */
    private int max_tokens;


    /**
     * 控制生成文本的创造性，较高的值增加随机性，较低的值使生成更加确定性。
     */
    private int temperature;


    /**
     *  仅考虑概率累积最高的 tokens，而忽略其他概率较低的 tokens。
     */
    private int top_p;


    /**
     * 生成多少个不同的文本样本。
     */
    private int n;


    /**
     * 指定是否启用流式传输，即是否在生成文本时逐步接收输出。
     */
    private Boolean stream;


    /**
     *  是否返回每个 token 的对数概率。
     */
    private String logprobs;


    /**
     *  指定在生成文本时的停止条件，即遇到哪个字符串时停止生成。
     */
    private String stop;

}
