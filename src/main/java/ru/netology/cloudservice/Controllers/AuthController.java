package ru.netology.cloudservice.Controllers;

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
            Map<String, String> response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("auth-token") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok("Logged out successfully");
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid token");
        }
    }
}
