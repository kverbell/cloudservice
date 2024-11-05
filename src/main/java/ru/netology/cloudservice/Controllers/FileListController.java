package ru.netology.cloudservice.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Services.FileService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/list")
public class FileListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListController.class);

    private final FileService fileService;
    private final TokenProvider tokenProvider;

    public FileListController(FileService fileService, TokenProvider tokenProvider) {
        this.fileService = fileService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDTO>> listFiles(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        logRequest("listFiles", token, null, null);

        String login = validateTokenAndGetLogin(sanitizeToken(token));
        List<FileResponseDTO> files = fileService.getAllFiles(login, limit);

        if (files.isEmpty()) {
            LOGGER.info("Пользователь с логином {} не имеет загруженных файлов", login);
            return ResponseEntity.ok(Collections.emptyList());
        }

        LOGGER.debug("Список файлов получен пользователем {}", login);
        return ResponseEntity.ok(files);
    }

    private void logRequest(String methodName, String token, String fileName, Long fileSize) {
        LOGGER.debug("Запрос к методу {}: Токен: {}, Имя файла: {}, Размер файла: {}",
                methodName, token != null && !token.isEmpty(), fileName, fileSize);
    }

    private String sanitizeToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    private String validateTokenAndGetLogin(String token) {
        LOGGER.debug("Валидный токен {}", token);

        if (token.isEmpty()) {
            LOGGER.warn("Токен отсутствует");
            throw new UnauthorizedException("Требуется токен аутентификации");
        }

        String login = tokenProvider.getLoginFromToken(token);
        if (login == null) {
            LOGGER.warn("Невалидный токен: {}", token);
            throw new UnauthorizedException("Неверный токен");
        }

        LOGGER.debug("Токен подтвержден, логин: {}", login);
        return login;
    }
}
