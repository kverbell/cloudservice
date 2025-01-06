package ru.netology.cloudservice.Controllers;

import ru.netology.cloudservice.Exceptions.InvalidLoginOrPasswordException;
import ru.netology.cloudservice.Services.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import lombok.NonNull;

import java.util.*;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final @NonNull AuthService authService;

    public AuthController(@NonNull AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/login", "/cloud/login", "/"})
    public ResponseEntity<Map<String, Object>> login(@NonNull @RequestBody LoginRequest loginRequest) {
        LOGGER.debug("Получен запрос на вход: {}", loginRequest);

        if (isLoginRequestInvalid(loginRequest)) {
            LOGGER.warn("Запрос на вход недействителен: {}", loginRequest);
            throw new InvalidLoginOrPasswordException("Логин или пароль не могут быть пустыми", "both");
        }

        LOGGER.info("Попытка аутентификации пользователя: {}", loginRequest.getLogin());

        Map<String, String> responseMap = authService.login(loginRequest.getLogin(), loginRequest.getPassword());
        String authToken = responseMap.get("auth-token");
        LOGGER.info("Аутентификация успешна для пользователя: {}", loginRequest.getLogin());

        return ResponseEntity.ok(Map.of("auth-token", authToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutPost(@NonNull HttpServletRequest request) {
        return handleLogout(request);
    }

    @GetMapping("/login?logout")
    public ResponseEntity<Void> logoutGet(@NonNull HttpServletRequest request) {
        return handleLogout(request);
    }

    private ResponseEntity<Void> handleLogout(HttpServletRequest request) {
        String token = request.getHeader("auth-token");
        if (token == null) {
            LOGGER.warn("Запрос на выход не содержит токен.");
            return ResponseEntity.badRequest().build();
        }
        LOGGER.info("Попытка выхода с токеном: {}", token);
        authService.logout(token);
        LOGGER.info("Выход успешен для токена: {}", token);
        return ResponseEntity.ok().build();
    }

    private boolean isLoginRequestInvalid(LoginRequest loginRequest) {
        return loginRequest.getLogin() == null || loginRequest.getLogin().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty();
    }
}