package com.tokensTool.pandoraNext.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plexpt.chatgpt.entity.chat.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    private String model;

    private List<Message> messages;

    private Boolean stream;

    private int temperature;

    @JsonProperty("presence_penalty")
    private int presencePenalty;

}
