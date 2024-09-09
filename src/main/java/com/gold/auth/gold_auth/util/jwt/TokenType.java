package com.gold.auth.gold_auth.util.jwt;

import lombok.Getter;

@Getter
public enum TokenType {
    AT("access"),
    RT("refresh");

    String type;

    TokenType(String type) {
        this.type = type;
    }
}
