//package ru.netology.cloudservice;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//import ru.netology.cloudservice.Controllers.AuthController;
//import ru.netology.cloudservice.Controllers.LoginRequest;
//import ru.netology.cloudservice.Exceptions.UnauthorizedException;
//import ru.netology.cloudservice.Services.AuthService;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//class AuthControllerTest {
//
//    @Mock
//    private AuthService authService;
//
//    @InjectMocks
//    private AuthController authController;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void login_Success() {
//
//        LoginRequest loginRequest = new LoginRequest("username", "password");
//        Map<String, String> expectedResponse = new HashMap<>();
//        expectedResponse.put("token", "dummy-token");
//
//        Mockito.when(authService.login(loginRequest.getUsername(), loginRequest.getPassword()))
//                .thenReturn(expectedResponse);
//
//        ResponseEntity<Map<String, String>> response = authController.login(loginRequest);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertEquals("dummy-token", Objects.requireNonNull(response.getBody()).get("token"));
//    }
//
//    @Test
//    void login_InvalidCredentials() {
//
//        LoginRequest loginRequest = new LoginRequest("username", "wrong-password");
//
//        Mockito.when(authService.login(loginRequest.getUsername(), loginRequest.getPassword()))
//                .thenThrow(new RuntimeException("Invalid credentials"));
//
//        Assertions.assertThrows(UnauthorizedException.class, () -> authController.login(loginRequest));
//    }
//
//    @Test
//    void logout_Success() {
//
//        String token = "valid-token";
//
//        ResponseEntity<String> response = authController.logout(token);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertEquals("Logged out successfully", response.getBody());
//    }
//
//    @Test
//    void logout_InvalidToken() {
//
//        String token = "invalid-token";
//
//        Mockito.doThrow(new RuntimeException("Invalid token"))
//                .when(authService).logout(token);
//
//        Assertions.assertThrows(UnauthorizedException.class, () -> authController.logout(token));
//    }
//}
