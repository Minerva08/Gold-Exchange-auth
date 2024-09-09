package com.gold.auth.gold_auth.util.jwt;


import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long accessExpiration;
    private final Long refreshExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validate-in-seconds}") String accessExpiration,
        @Value("${jwt.refresh-token-validate-in-seconds}") String refreshExpiration) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
            Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpiration = Long.parseLong(accessExpiration) * 1000;
        this.refreshExpiration = Long.parseLong(refreshExpiration) * 1000;
    }

    public String getUserId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", String.class);
    }


    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
            .get("category", String.class);
    }

    public String createJwt(String category, String userId) {
        return Jwts.builder()
            .claim("category", category)
            .claim("userId", userId)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(
                System.currentTimeMillis() + (Objects.equals(category, TokenType.RT.getType()) ? refreshExpiration
                    : accessExpiration)))
            .signWith(secretKey)
            .compact();
    }
}
