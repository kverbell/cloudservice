package ru.netology.cloudservice.Configuration;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenProvider {

    private final Map<String, String> tokens = new HashMap<>();

    public String createToken(String username) {
        String token = username + "-token";
        tokens.put(token, username);
        return token;
    }

    public void invalidateToken(String token) {
        tokens.remove(token);
    }

    public boolean validateToken(String token) {
        return tokens.containsKey(token);
    }

    public String getUsernameFromToken(String token) {
        return tokens.get(token);
    }
}
