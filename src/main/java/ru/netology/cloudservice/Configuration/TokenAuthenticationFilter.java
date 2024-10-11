package ru.netology.cloudservice.Configuration;

import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public TokenAuthenticationFilter(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        LOGGER.debug("Входящий запрос на URL: {}", request.getRequestURI());

        if (request.getRequestURI().equals("/login")) {
            LOGGER.debug("Обнаружен запрос на вход в систему.");
            chain.doFilter(request, response);
            return;
        }

        String token = extractAuthToken(request);

        try {
            if (tokenProvider.validateToken(token)) {
                String login = tokenProvider.getLoginFromToken(token);
                LOGGER.debug("Извлечен логин: {}", login);

                Optional<User> userOpt = userRepository.findByUsername(login);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.getUsername(), null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    LOGGER.debug("Установлен контекст проверки подлинности для логина: {}", login);
                } else {
                    LOGGER.debug("Пользователя с логином {} не существует, возвращается 401", login);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Неправильный логин или пароль");
                    return;
                }
            } else {
                LOGGER.warn("Неверный токен: {}", token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Неверный токен");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка в фильтре аутентификации: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Внутренняя ошибка сервера");
            return;
        }

        LOGGER.debug("Передача запроса по цепочке фильтров после успешной аутентификации");
        chain.doFilter(request, response);
    }

    private String extractAuthToken(HttpServletRequest request) {
        String token = request.getHeader("auth-token");

        if (token != null) {
            LOGGER.debug("Из 'auth-token' извлечен токен: {}", token);
        } else {
            LOGGER.warn("'auth-token' не найден");
        }

        if (token != null && token.startsWith("Bearer ")) {
            LOGGER.debug("Из токена убран префикс 'Bearer'");
            token = token.substring(7);
        } else {
            LOGGER.debug("Токен не содержит префикс 'Bearer'");
        }

        return token;
    }
}
