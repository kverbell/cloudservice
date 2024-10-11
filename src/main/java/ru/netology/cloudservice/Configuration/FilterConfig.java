package ru.netology.cloudservice.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }
}