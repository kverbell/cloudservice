package ru.netology.cloudservice.Controllers;

import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Exceptions.FileNotFoundException;
import ru.netology.cloudservice.Exceptions.FileSizeExceededException;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Services.FileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.apache.tomcat.util.http.fileupload.FileUploadException;

import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final TokenProvider tokenProvider;

    public FileController(FileService fileService, TokenProvider tokenProvider) {
        this.fileService = fileService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping
    public ResponseEntity<FileResponseDTO> uploadFile(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam("filename") @NotNull String fileName,
            @RequestPart("file") MultipartFile file) throws FileUploadException {

        logRequest("uploadFile", token, fileName, String.valueOf(file.getSize()));

        validateFileName(fileName);

        if (file.getSize() > 100 * 1024 * 1024) {
            LOGGER.warn("Файл слишком большой: {} МБ", file.getSize());
            throw new FileSizeExceededException("Файл слишком большой. Максимальный размер: 100MB");
        }

        String login = validateTokenAndGetLogin(sanitizeToken(token));

        try {
            fileService.addFile(fileName, login, file);
            double fileSize = file.getSize();
            LOGGER.debug("Файл {} размером {} успешно загружен пользователем {}", fileName, file.getSize(), login);
            return ResponseEntity.ok(new FileResponseDTO(fileName, fileSize));
        } catch (IOException e) {
            LOGGER.error("Ошибка при загрузке файла: {}", e.getMessage());
            throw new FileUploadException("Ошибка при загрузке файла: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Необработанное исключение: {}", e.getMessage(), e);
            throw new FileUploadException("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<String> updateFileName(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam("filename") String currentFileName,
            @RequestParam(value = "newName", required = false) String newFileName) {

        logRequest("updateFileName", token, currentFileName, newFileName);

        String login = validateTokenAndGetLogin(sanitizeToken(token));

        try {
            if (newFileName == null || newFileName.isEmpty()) {
                LOGGER.info("Новое имя не передано. Генерируем уникальное имя для файла '{}'", currentFileName);
                newFileName = fileService.generateUniqueFileName(currentFileName, login);
            }

            LOGGER.info("Пользователь с логином {} пытается переименовать файл '{}' на '{}'", login, currentFileName, newFileName);

            validateFileName(newFileName);
            fileService.updateFileName(currentFileName, newFileName, login);

            return ResponseEntity.ok("Файл успешно переименован на " + newFileName);

        } catch (Exception e) {
            LOGGER.error("Ошибка при переименовании файла '{}' на '{}': {}", currentFileName, newFileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при переименовании файла");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam("filename") String fileName) {

        logRequest("deleteFile", token, fileName, null);

        validateFileName(fileName);

        String login = validateTokenAndGetLogin(sanitizeToken(token));
        fileService.deleteFile(fileName, login);
        LOGGER.debug("Файл {} успешно удален пользователем {}", fileName, login);
        return ResponseEntity.ok("Файл успешно удален");
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty() || fileName.length() > 255) {
            throw new IllegalArgumentException("Недопустимое имя файла");
        }
    }

    @GetMapping
    public ResponseEntity<Resource> getFile(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam("filename") String fileName) {

        logRequest("getFile", token, fileName, null);

        validateFileName(fileName);

        String login = validateTokenAndGetLogin(sanitizeToken(token));
        FileData fileData = fileService.getFileByNameAndUserLogin(fileName, login);
        LOGGER.debug("Файл {} успешно получен пользователем {}", fileName, login);//

        Path filePath = Paths.get(fileData.getFilePath());

        if (Files.notExists(filePath)) {
            throw new FileNotFoundException("Файл не найден на сервере");
        }

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    protected void logRequest(String methodName, String token, String fileName, String fileSize) {
        LOGGER.debug("Запрос к методу {}: Токен: {}, Имя файла: {}, Размер файла: {}",
                methodName, token != null && !token.isEmpty(), fileName, fileSize);
    }

    protected String sanitizeToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    protected String validateTokenAndGetLogin(String token) {
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
