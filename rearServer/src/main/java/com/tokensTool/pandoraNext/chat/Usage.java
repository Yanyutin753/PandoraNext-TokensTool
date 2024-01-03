package com.tokensTool.pandoraNext.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usage {

    /**
     * 提示词消耗的tokens
     */
    @JsonProperty("prompt_tokens")
    private long promptTokens;

    /**
     * 回答消耗的tokens
     */
    @JsonProperty("completion_tokens")
    private long completionTokens;

    /**
     * 本次对话消耗的tokens
     */
    @JsonProperty("total_tokens")
    private long totalTokens;


}
