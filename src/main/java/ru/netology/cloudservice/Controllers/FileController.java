package ru.netology.cloudservice.Controllers;

import ru.netology.cloudservice.Configuration.TokenProvider;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.UnauthorizedException;
import ru.netology.cloudservice.Repositories.UserRepository;
import ru.netology.cloudservice.Services.FileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public FileController(FileService fileService, TokenProvider tokenProvider, UserRepository userRepository) {
        this.fileService = fileService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") String token,
                                             @RequestParam("filename") String fileName,
                                             @RequestPart("file") MultipartFile file) throws IOException {
        Long userId = getUserIdFromToken(token);
        fileService.addFile(fileName, file.getBytes(), userId);
        return ResponseEntity.ok("Файл успешно загружен");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") String token,
                                             @RequestParam("filename") String fileName) {
        Long userId = getUserIdFromToken(token);
        fileService.deleteFile(fileName, userId);
        return ResponseEntity.ok("Файл успешно удален");
    }

    @GetMapping
    public ResponseEntity<byte[]> getFile(@RequestHeader("auth-token") String token,
                                          @RequestParam("filename") String fileName) {
        Long userId = getUserIdFromToken(token);
        FileData fileData = fileService.getFileByName(fileName, userId);
        return ResponseEntity.ok(fileData.getFileContent());
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileData>> listFiles(@RequestHeader("auth-token") String token,
                                                    @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long userId = getUserIdFromToken(token);
        List<FileData> files = fileService.getAllFiles(userId);
        return ResponseEntity.ok(files);
    }

    private Long getUserIdFromToken(String token) {
        String username = tokenProvider.getUsernameFromToken(token);
        if (username == null) {
            throw new UnauthorizedException("Недопустимый токен");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UnauthorizedException("Пользователь не найден");
        }

        return userOpt.get().getId();
    }
}
