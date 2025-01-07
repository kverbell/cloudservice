import ru.netology.cloudservice.Controllers.AuthController;
import ru.netology.cloudservice.Controllers.LoginRequest;
import ru.netology.cloudservice.Exceptions.InvalidLoginOrPasswordException;
import ru.netology.cloudservice.Services.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//Тесты запускаются с помощью mvn clean test
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");
        when(authService.login("user", "password")).thenReturn(Map.of("auth-token", "test-token"));

        // Act
        ResponseEntity<Map<String, Object>> response = authController.login(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().get("auth-token"));
        verify(authService, times(1)).login("user", "password");
    }

    @Test
    void testLogin_InvalidRequest_ThrowsException() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("", "");

        // Act & Assert
        InvalidLoginOrPasswordException exception = assertThrows(
                InvalidLoginOrPasswordException.class,
                () -> authController.login(invalidRequest)
        );

        assertEquals("Логин или пароль не могут быть пустыми", exception.getMessage());
    }

    @Test
    void testLogoutPost_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("auth-token")).thenReturn("valid-token");

        // Act
        ResponseEntity<Void> response = authController.logoutPost(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).logout("valid-token");
    }

    @Test
    void testLogoutPost_NoToken_BadRequest() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("auth-token")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = authController.logoutPost(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(authService, never()).logout(any());
    }

    @Test
    void testLogoutGet_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("auth-token")).thenReturn("valid-token");

        // Act
        ResponseEntity<Void> response = authController.logoutGet(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).logout("valid-token");
    }
}
