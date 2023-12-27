package com.tokensTool.pandoraNext.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionResult {

   private String id;

   private String object;

   private Long created;

   private String model;

   private List<Choice> choices;

   private Usage usage;

   private String system_fingerprint;

}
