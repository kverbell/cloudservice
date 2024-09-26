package ru.netology.cloudservice.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);

    private final Map<String, String> tokens = new HashMap<>();


    public String createToken(String login) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, login);
        LOGGER.debug("Creating token for user {}", login);
        LOGGER.debug("Token added to collection: {}", token);
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            LOGGER.warn("Token is null or empty"); // Изменим на warn для большей серьезности
            return false;
        }
        token = token.replace("auth-token: ", "");
        LOGGER.debug("Validating token {}", token);
        boolean isValid = tokens.containsKey(token);
        LOGGER.info("Token {} validation result: {}", token, isValid);
        return isValid;
    }

    public String getLoginFromToken(String token) {
        LOGGER.debug("Getting login from token {}", token);
        return tokens.get(token);
    }

    public void invalidateToken(String token) {
        LOGGER.debug("Invalidating token {}", token);
        tokens.remove(token);
    }
}