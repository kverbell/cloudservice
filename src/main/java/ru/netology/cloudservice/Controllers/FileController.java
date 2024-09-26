package ru.netology.cloudservice.Controllers;

import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Repositories.UserRepository;
import ru.netology.cloudservice.Services.FileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final TokenProvider tokenProvider;

    public FileController(FileService fileService, TokenProvider tokenProvider, UserRepository userRepository) {
        this.fileService = fileService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                             @RequestParam("filename") @NotNull String fileName,
                                             @RequestPart("file") MultipartFile file) throws IOException {
        LOGGER.debug("Upload request for file: {}", fileName);
        String login = validateTokenAndGetLogin(token);
        fileService.addFile(fileName, file.getBytes(), login);
        LOGGER.debug("File {} successfully uploaded by user {}", fileName, login);
        return ResponseEntity.ok("Файл успешно загружен");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                             @RequestParam("filename") String fileName) {
        LOGGER.debug("Delete request for file: {}", fileName);
        String login = validateTokenAndGetLogin(token);
        fileService.deleteFile(fileName, login);
        LOGGER.debug("File {} successfully deleted by user {}", fileName, login);
        return ResponseEntity.ok("Файл успешно удален");
    }

    @GetMapping
    public ResponseEntity<byte[]> getFile(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                          @RequestParam("filename") String fileName) {
        LOGGER.debug("Download request for file: {}", fileName);
        String login = validateTokenAndGetLogin(token);
        FileData fileData = fileService.getFileByNameAndUserLogin(fileName, login);
        LOGGER.debug("File {} successfully retrieved for user {}", fileName, login);
        return ResponseEntity.ok(fileData.getFileContent());
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileData>> listFiles(@RequestHeader(value = "auth-token", defaultValue = "") String token,
                                                    @RequestParam(value = "limit", defaultValue = "10") int limit) {
        LOGGER.debug("Listing files request with limit {}", limit);
        String login = validateTokenAndGetLogin(token);
        List<FileData> files = fileService.getAllFiles(login);
        LOGGER.debug("File list successfully retrieved for user {}", login);
        return ResponseEntity.ok(files);
    }

    private String validateTokenAndGetLogin(String token) {
        LOGGER.debug("Validating token {}", token);
        if (token.isEmpty()) {
            LOGGER.warn("Token is missing");
            throw new UnauthorizedException("Требуется токен аутентификации");
        }
        String login = tokenProvider.getLoginFromToken(token);
        if (login == null) {
            LOGGER.warn("Invalid token: {}", token);
            throw new UnauthorizedException("Неверный токен");
        }
        LOGGER.debug("Token validated successfully, login: {}", login);
        return login;
    }
}
