package ru.netology.cloudservice.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        LOGGER.info("Получен запрос: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            LOGGER.info("Header: {} = {}", headerName, httpRequest.getHeader(headerName));
        });

        chain.doFilter(request, response);
    }
}
