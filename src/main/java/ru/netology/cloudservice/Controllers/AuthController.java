package ru.netology.cloudservice.Controllers;

import org.springframework.http.HttpStatus;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Services.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, String> response = authService.login(loginRequest.getLogin(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Неверные учетные данные");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "auth-token", defaultValue = "") String token) {
        try {
            if (token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Требуется токен аутентификации");
            }
            authService.logout(token);
            return ResponseEntity.ok("Выход из системы");
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Неверный токен");
        }
    }
}
