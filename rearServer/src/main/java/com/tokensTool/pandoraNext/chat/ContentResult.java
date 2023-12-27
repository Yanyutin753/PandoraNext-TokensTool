package com.tokensTool.pandoraNext.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 内容补全返回结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentResult {

    private String id;

    private String object;

    private Long created;

    private String model;

    private List<ContentChoice> choices;

    private Usage usage;

}
