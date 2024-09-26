package ru.netology.cloudservice.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.netology.cloudservice.Services.AuthService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final TokenProvider tokenProvider;
    private final AuthService authService;

    public SecurityConfig(TokenProvider tokenProvider, AuthService authService) {
        this.tokenProvider = tokenProvider;
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        LOGGER.info("Configuring Security Filter Chain...");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/logout", "/cloud/login").permitAll()  // Открытые пути
                        .anyRequest().authenticated()  // Остальные запросы требуют аутентификации
                )
                .csrf(AbstractHttpConfigurer::disable) // Отключение CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Настройка CORS
                .addFilterBefore(new TokenAuthenticationFilter(tokenProvider, authService), BasicAuthenticationFilter.class); // Добавление фильтра
        LOGGER.info("Security Filter Chain configured successfully.");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        LOGGER.info("Configuring CORS...");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8080"); // Убедитесь, что это адрес вашего фронтенда
        configuration.addAllowedMethod("*"); // Разрешение всех методов
        configuration.setAllowCredentials(true); // Разрешение отправки куков
        configuration.addAllowedHeader("*"); // Разрешение всех заголовков
        configuration.addExposedHeader("Authorization"); // Экспонирование заголовка Authorization

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Настройка для всех путей

        LOGGER.info("CORS configuration done.");

        return source;
    }
}