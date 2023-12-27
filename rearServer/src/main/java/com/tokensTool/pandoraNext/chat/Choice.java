package com.tokensTool.pandoraNext.chat;

import com.plexpt.chatgpt.entity.chat.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Choice {

    private long index;

    private Message message;

    private String finish_reason;

}
