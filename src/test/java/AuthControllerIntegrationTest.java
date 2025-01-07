import ru.netology.cloudservice.Controllers.AuthController;
import ru.netology.cloudservice.Controllers.LoginRequest;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.*;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Тесты запускаются с помощью mvn clean test
@Testcontainers
@SpringBootTest
class AuthControllerIntegrationTest {

    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    private final AuthController authController;
    private final UserRepository userRepository;

    public AuthControllerIntegrationTest(AuthController authController, UserRepository userRepository) {
        this.authController = authController;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("password");
        userRepository.save(user);
    }

    @Test
    void testLogin_Success_Integration() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");

        // Act
        ResponseEntity<Map<String, Object>> response = authController.login(loginRequest);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().containsKey("auth-token"));
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException_Integration() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("invalid", "invalid");

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.login(invalidRequest)
        );

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());  // Проверка статуса ошибки

        String reason = exception.getReason();
        assert reason != null;
        Assertions.assertTrue(reason.contains("Неверный пароль") || reason.contains("Некорректный адрес электронной почты"));
    }

    @Test
    void testUnauthorizedException_Integration() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Неавторизованный доступ");

        // Act & Assert
        ResponseStatusException thrownException = assertThrows(
                ResponseStatusException.class,
                () -> authController.logoutPost(mock(HttpServletRequest.class))
        );

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, thrownException.getStatusCode());
        Assertions.assertEquals("Неавторизованный доступ", thrownException.getReason());
    }

    @Test
    void testAccessDeniedException_Integration() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Отказ в доступе");

        // Act & Assert
        ResponseStatusException thrownException = assertThrows(
                ResponseStatusException.class,
                () -> authController.logoutPost(mock(HttpServletRequest.class))
        );

        Assertions.assertEquals(HttpStatus.FORBIDDEN, thrownException.getStatusCode());
        Assertions.assertEquals("Отказ в доступе", thrownException.getReason());
    }

    @Test
    void testLogoutPost_Success_Integration() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("auth-token")).thenReturn("valid-token");

        // Act
        ResponseEntity<Void> response = authController.logoutPost(request);

        // Assert
        Assertions.assertEquals(200, response.getStatusCodeValue());
    }
}