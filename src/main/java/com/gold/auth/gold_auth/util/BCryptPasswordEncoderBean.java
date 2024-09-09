package com.gold.auth.gold_auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderBean {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderBean() {
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * 비밀번호 암호화
     */
    public String encodePassword(String password) {
        return encoder.encode(password);
    }

    /**
     * 비밀번호 검증
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}
