package ru.netology.cloudservice.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLoginException(InvalidLoginException ex) {
        LOGGER.warn("Ошибка входа: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        LOGGER.warn("Пользователь не найден: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 404);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        LOGGER.warn("Неавторизованный доступ: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 401);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        LOGGER.warn("Отказ в доступе: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 403);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        LOGGER.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Внутренняя ошибка сервера", 500);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleFileNotFoundException(FileNotFoundException ex) {
        LOGGER.error("Файл не найден: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<String> handleFileAlreadyExistsException(FileAlreadyExistsException ex) {
        LOGGER.error("Файл уже существует: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        LOGGER.error("Ресурс не найден: {}, Request: {}", ex.getMessage(), request.getDescription(false));
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 404);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>("Метод не поддерживается для этого запроса", HttpStatus.METHOD_NOT_ALLOWED);
    }
}
