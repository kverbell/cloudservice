package ru.netology.cloudservice.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.InvalidLoginException;
import ru.netology.cloudservice.Exceptions.UserNotFoundException;
import ru.netology.cloudservice.Repositories.UserRepository;
import ru.netology.cloudservice.Configuration.TokenProvider;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        if (login == null || password == null) {
            LOGGER.error("Логин или пароль не могут быть null");
            throw new InvalidLoginException("Нужно ввести логин и пароль");
        }

        LOGGER.debug("Попытка входа для пользователя: {}", login);

        Optional<User> userOpt = userRepository.findByUsername(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LOGGER.debug("Пользователь найден: {}", user.getUsername());

            if (password.equals(user.getPassword())) {
                String token = tokenProvider.createToken(login);
                LOGGER.info("Аутентификация успешна для пользователя: {}", login);
                return Map.of("auth-token", token);
            } else {
                LOGGER.warn("Неверный пароль для пользователя: {}", login);
                throw new InvalidLoginException("Неверный логин или пароль");
            }
        }

        LOGGER.warn("Пользователь не найден: {}", login);
        throw new UserNotFoundException("Пользователь не найден");
    }

    public void logout(String token) {
        tokenProvider.invalidateToken(token);
    }

    public boolean existsByLogin(String login) {
        return userRepository.findByUsername(login).isPresent();
    }

    public UserDetails loadUserByUsername(String login) {
        Optional<User> userOpt = userRepository.findByUsername(login);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
        }
        throw new UsernameNotFoundException("Пользователь не найден");
    }
}