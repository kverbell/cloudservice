package ru.netology.cloudservice.Controllers;

import org.springframework.http.HttpStatus;
import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Services.FileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final TokenProvider tokenProvider;

    public FileController(FileService fileService, TokenProvider tokenProvider) {
        this.fileService = fileService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestPart("file") MultipartFile file) {
        LOGGER.debug("Запрос на загрузку файла: {}", fileName);

        LOGGER.debug("Размер полученного файла: {}", file.getSize());
        LOGGER.debug("Тип полученного файла: {}", file.getContentType());

        String login = validateTokenAndGetLogin(token);

        if (file.getSize() > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер: 100MB");
        }

        try {
            fileService.addFile(fileName, file.getBytes(), login);
            LOGGER.debug("Файл {} успешно загружен пользователем {}", fileName, login);
            return ResponseEntity.ok("Файл успешно загружен");
        } catch (IOException e) {
            LOGGER.error("Ошибка при загрузке файла: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                             @RequestParam("filename") String fileName) {
        LOGGER.debug("Запрос на удаление файла: {}", fileName);
        String login = validateTokenAndGetLogin(token);
        fileService.deleteFile(fileName, login);
        LOGGER.debug("Файл {} успешно удален пользователем {}", fileName, login);
        return ResponseEntity.ok("Файл успешно удален");
    }

    @GetMapping
    public ResponseEntity<byte[]> getFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                          @RequestParam("filename") String fileName) {
        LOGGER.debug("Запрос на получение файла: {}", fileName);
        if (token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        String login = validateTokenAndGetLogin(token);
        FileData fileData = fileService.getFileByNameAndUserLogin(fileName, login);
        LOGGER.debug("Файл {} успешно получен пользователем {}", fileName, login);
        return ResponseEntity.ok(fileData.getFileContent());
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileResponseDTO>> listFiles(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        LOGGER.debug("Запрос списка файлов с ограничением {}", limit);
        String login = validateTokenAndGetLogin(token);
        List<FileResponseDTO> files = fileService.getAllFiles(login);

        if (files.isEmpty()) {
            LOGGER.info("Пользователь с логином {} не имеет загруженных файлов", login);
            return ResponseEntity.ok(Collections.emptyList());
        }

        LOGGER.debug("Список файлов получен пользователем {}", login);
        return ResponseEntity.ok(files);
    }

    private String validateTokenAndGetLogin(String token) {
        LOGGER.debug("Валидный токен {}", token);

        if (token.isEmpty()) {
            LOGGER.warn("Токен отсутствует");
            throw new UnauthorizedException("Требуется токен аутентификации");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
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
