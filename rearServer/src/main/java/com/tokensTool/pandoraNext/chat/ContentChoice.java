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
public class ContentChoice {

    private String text;


    private int index;


    private String logprobs;


    private String finish_reason;




}
