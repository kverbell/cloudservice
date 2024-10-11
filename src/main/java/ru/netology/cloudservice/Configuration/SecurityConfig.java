package ru.netology.cloudservice.Configuration;

import ru.netology.cloudservice.Repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public SecurityConfig(@Lazy TokenProvider tokenProvider,
                          @Lazy UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.info("Configuring Security Filter Chain...");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/logout", "/cloud/login").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .addFilterBefore(new TokenAuthenticationFilter(tokenProvider, userRepository), BasicAuthenticationFilter.class);

        LOGGER.info("Security Filter Chain configured successfully.");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        LOGGER.info("Configuring CORS...");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://192.168.31.89:8080/");
        configuration.addAllowedMethod("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("auth-token");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        LOGGER.info("CORS configuration done.");
        return source;
    }
}