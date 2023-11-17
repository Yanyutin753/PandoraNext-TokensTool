package com.yyandywt99.pandoraNext.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class token {
    private String name;
    private String token;
    private String username;
    private String userPassword;
    private boolean shared;
    private boolean show_user_info;
    private boolean plus;
    private String password;
    private String updateTime;
}