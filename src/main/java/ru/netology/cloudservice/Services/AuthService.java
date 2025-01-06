package ru.netology.cloudservice.Services;

import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.InvalidLoginOrPasswordException;
import ru.netology.cloudservice.Repositories.UserRepository;
import ru.netology.cloudservice.Configuration.TokenProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public Map<String, String> login(String login, String password) {
        if (login == null || password == null || login.trim().isEmpty() || password.trim().isEmpty()) {
            LOGGER.error("Логин или пароль не могут быть null или пустыми");
            throw new InvalidLoginOrPasswordException("Нужно ввести логин и пароль", "both");
        }

        LOGGER.debug("Попытка входа для пользователя: {}", login);

        Optional<User> userOpt = userRepository.findByUsername(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LOGGER.debug("Пользователь найден: {}", user.getUsername());

            if (user.getPassword().equals(password)) {
                String token = tokenProvider.createToken(login);
                LOGGER.info("Аутентификация успешна для пользователя: {}", login);
                return Map.of("auth-token", token);
            } else {
                LOGGER.warn("Неверный пароль для пользователя: {}", login);
                throw new InvalidLoginOrPasswordException("Неверный пароль", "password");
            }
        }

        LOGGER.warn("Пользователь не найден: {}", login);
        throw new InvalidLoginOrPasswordException("Пользователь не найден", "email");
    }

    public void logout(String token) {
        tokenProvider.invalidateToken(token);
    }
}