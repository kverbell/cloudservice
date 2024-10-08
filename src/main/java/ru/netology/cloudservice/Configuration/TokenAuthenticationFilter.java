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
        LOGGER.debug("Incoming request to URI: {}", request.getRequestURI());

        if (request.getRequestURI().equals("/login")) {
            LOGGER.debug("Login request detected, passing through filter chain.");
            chain.doFilter(request, response);
            return;
        }

        String token = extractAuthToken(request);

        if (token == null) {
            LOGGER.debug("Token not found in request");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found in request");
            return;
        }

        try {
            if (tokenProvider.validateToken(token)) {
                String login = tokenProvider.getLoginFromToken(token);
                LOGGER.debug("Extracted login: {}", login);

                Optional<User> userOpt = userRepository.findByUsername(login);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.getUsername(), null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    LOGGER.debug("Set authentication context for login: {}", login);
                } else {
                    LOGGER.debug("User with login {} does not exist, returning 401", login);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Неправильный логин или пароль");
                    return;
                }
            } else {
                LOGGER.warn("Invalid token: {}", token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка в фильтре аутентификации: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Внутренняя ошибка сервера");
            return;
        }

        LOGGER.debug("Passing request through filter chain after successful authentication");
        chain.doFilter(request, response);
    }

    private String extractAuthToken(HttpServletRequest request) {
        return request.getHeader("auth-token");
    }
}
