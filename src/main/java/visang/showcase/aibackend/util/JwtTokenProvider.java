package visang.showcase.aibackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String MEMBER_NO_KEY = "memberNo";

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Map<String, Object> claims) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    private Claims extractBody(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Map<String, Object> getPayload(String token) {
        try {
            final Claims body = extractBody(token);

            return body.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } catch (JwtException | IllegalArgumentException e) {
            return Map.of("message", new String("유효하지 않은 토큰입니다."));
        }
    }

    public String getMemberNo(String token) {
        Map<String, Object> claims = getPayload(token);
        return String.valueOf(claims.get(MEMBER_NO_KEY));
    }
}