package ru.netology.cloudservice.Controllers;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Exceptions.FileSizeExceededException;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Services.FileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tomcat.util.http.fileupload.FileUploadException;

import jakarta.validation.constraints.NotNull;

import java.io.IOException;

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

        logRequest("uploadFile", token, fileName, file.getSize());

        validateFileName(fileName);

        if (file.getSize() > 100 * 1024 * 1024) {
            LOGGER.warn("Файл слишком большой: {} байт", file.getSize());
            throw new FileSizeExceededException("Файл слишком большой. Максимальный размер: 100MB");
        }

        String login = validateTokenAndGetLogin(sanitizeToken(token));

        try {
            fileService.addFile(fileName, file.getBytes(), login);
            LOGGER.debug("Файл {} успешно загружен пользователем {}", fileName, login);
            return ResponseEntity.ok(new FileResponseDTO(fileName, file.getSize()));
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
            @RequestParam("newName") String newFileName) {

        logRequest("updateFileName", token, currentFileName, null);

        validateFileName(newFileName);

        String login = validateTokenAndGetLogin(sanitizeToken(token));

        fileService.updateFileName(currentFileName, newFileName, login);
        LOGGER.debug("Файл {} переименован в {} пользователем {}", currentFileName, newFileName, login);

        return ResponseEntity.ok("Файл успешно переименован");
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
    public ResponseEntity<byte[]> getFile(
            @RequestHeader(value = "auth-token", defaultValue = "") String token,
            @RequestParam("filename") String fileName) throws IOException {

        logRequest("getFile", token, fileName, null);

        validateFileName(fileName);

        String login = validateTokenAndGetLogin(sanitizeToken(token));
        FileData fileData = fileService.getFileByNameAndUserLogin(fileName, login);
        LOGGER.debug("Файл {} успешно получен пользователем {}", fileName, login);

        Resource resource = new ByteArrayResource(fileData.getFileContent());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource.getContentAsByteArray());
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
