package ru.netology.cloudservice.Controllers;

import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Services.FileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/list")
public class FileListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListController.class);

    private final FileService fileService;
    private final FileController fileController;

    public FileListController(FileService fileService, FileController fileController) {
        this.fileService = fileService;
        this.fileController = fileController;
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDTO>> listFiles(
            @RequestHeader(value = "auth-token", defaultValue = "") String token) {

        fileController.logRequest("listFiles", token, null, null);

        String login = fileController.validateTokenAndGetLogin(fileController.sanitizeToken(token));
        List<FileResponseDTO> files = fileService.getAllFiles(login);

        if (files.isEmpty()) {
            LOGGER.info("Пользователь с логином {} не имеет загруженных файлов", login);
            return ResponseEntity.ok(Collections.emptyList());
        }

        LOGGER.debug("Список файлов получен пользователем {}", login);
        return ResponseEntity.ok(files);
    }
}
