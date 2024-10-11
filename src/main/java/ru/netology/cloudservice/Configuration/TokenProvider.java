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
        LOGGER.debug("Создан токен для пользователя {}", login);
        LOGGER.debug("Токен добавлен к : {}", token);
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            LOGGER.warn("Токен является null или пустым");
            return false;
        }

        LOGGER.debug("Валидный токен {}", token);
        boolean isValid = tokens.containsKey(token);
        LOGGER.info("Результат проверки токена {}: {}", token, isValid);
        return isValid;
    }

    public String getLoginFromToken(String token) {
        LOGGER.debug("Получен логин из токена {}", token);
        return tokens.get(token);
    }

    public void invalidateToken(String token) {
        LOGGER.debug("Невалидный токен {}", token);
        tokens.remove(token);
    }
}