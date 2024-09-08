package ru.netology.cloudservice.Services;

import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Repositories.UserRepository;
import ru.netology.cloudservice.Configuration.TokenProvider;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public Map<String, String> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && password.equals(userOpt.get().getPassword())) {
            String token = tokenProvider.createToken(username);
            return Map.of("auth-token", token);
        }
        throw new RuntimeException("Неверное имя пользователя или пароль");
    }

    public void logout(String token) {
        tokenProvider.invalidateToken(token);
    }
}