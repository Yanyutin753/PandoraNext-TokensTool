package com.tokensTool.pandoraNext.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Model {

    private String id;

    private String object;

    private Long created;

    private String owned_by;

}
