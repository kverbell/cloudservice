package ru.netology.cloudservice.Services;

import ru.netology.cloudservice.Configuration.FileStorageProperties;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.FileResponseDTO;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.FileAlreadyExistsException;
import ru.netology.cloudservice.Exceptions.NoResourceFoundException;
import ru.netology.cloudservice.Repositories.FileDataRepository;
import ru.netology.cloudservice.Repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final FileDataRepository fileDataRepository;
    private final UserRepository userRepository;
    private final FileStorageProperties fileStorageProperties;

    private String uploadDir;

    public FileService(FileDataRepository fileDataRepository, UserRepository userRepository,
                       FileStorageProperties fileStorageProperties) {
        this.fileDataRepository = fileDataRepository;
        this.userRepository = userRepository;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostConstruct
    private void init() {
        this.uploadDir = fileStorageProperties.getUploadDir();
        LOGGER.info("Путь для загрузки файлов: {}", uploadDir);
        createUploadDir();
    }

    private void createUploadDir() {
        if (uploadDir == null || uploadDir.isEmpty()) {
            throw new RuntimeException("Директория для загрузки файлов не задана");
        }
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("Не удалось создать директорию для загрузки файлов");
            }
            LOGGER.info("Директория для загрузки файлов успешно создана: {}", uploadDir);
        } else {
            LOGGER.info("Директория для загрузки файлов уже существует: {}", uploadDir);
        }
    }

    public void updateFileName(String currentFileName, String newFileName, String login) {
        LOGGER.info("Пользователь с логином {} запрашивает переименование файла '{}' на '{}'", login, currentFileName, newFileName);
        User user = getUserByLogin(login);

        FileData fileData = fileDataRepository.findByFileNameAndUserId(currentFileName, user.getId())
                .orElseThrow(() -> new NoResourceFoundException("Файл с именем '" + currentFileName + "' не найден"));

        Optional<FileData> existingFile = fileDataRepository.findByFileNameAndUserId(newFileName, user.getId());
        if (existingFile.isPresent()) {
            LOGGER.error("Файл с именем '{}' уже существует для пользователя '{}'", newFileName, login);
            throw new FileAlreadyExistsException("Файл с таким именем уже существует");
        }

        fileData.setFileName(newFileName);
        fileDataRepository.save(fileData);
        LOGGER.info("Файл '{}' успешно переименован в '{}' для пользователя '{}'", currentFileName, newFileName, login);
    }

    public List<FileResponseDTO> getAllFiles(String login, int limit) {
        LOGGER.info("Пользователь с логином {} запрашивает список файлов с ограничением {}", login, limit);
        User user = getUserByLogin(login);

        List<FileData> fileDataList = fileDataRepository.findAllByUserId(user.getId())
                .stream()
                .limit(limit)
                .toList();

        List<FileResponseDTO> files = fileDataList.stream()
                .map(fileData -> new FileResponseDTO(fileData.getFileName(), fileData.getFileContent().length))
                .collect(Collectors.toList());

        LOGGER.info("Пользователь с логином {} получил {} файлов", login, files.size());
        return files;
    }

    public void addFile(String fileName, byte[] fileContent, String login) throws IOException {
        User user = getUserByLogin(login);
        LOGGER.debug("Пользователь {} загружает файл {}", login, fileName);

        Optional<FileData> existingFileOpt = fileDataRepository.findByFileNameAndUserId(fileName, user.getId());
        if (existingFileOpt.isPresent()) {
            LOGGER.error("Файл с именем {} уже существует для пользователя {}", fileName, login);
            throw new FileAlreadyExistsException("Файл с таким именем уже существует");
        }

        FileData fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFileContent(fileContent);
        fileData.setUser(user);

        fileDataRepository.save(fileData);
        LOGGER.info("Файл '{}' успешно загружен пользователем с логином {}", fileName, login);

        Path path = Paths.get(uploadDir, fileName);
        Files.write(path, fileContent);
        LOGGER.info("Файл сохранен по пути: {}", path);
    }

    public void deleteFile(String fileName, String login) {
        FileData fileData = getFileByNameAndUserLogin(fileName, login);

        fileDataRepository.delete(fileData);

        Path path = Paths.get(uploadDir, fileData.getFileName());

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error("Не удалось удалить файл с диска: {}", e.getMessage());
            throw new RuntimeException("Ошибка при удалении файла с диска");
        }

        LOGGER.info("Файл '{}' успешно удален пользователем с логином {}", fileName, login);
    }

    public FileData getFileByNameAndUserLogin(String fileName, String login) {
        LOGGER.info("Пользователь с логином {} запрашивает файл '{}'", login, fileName);
        User user = getUserByLogin(login);
        return fileDataRepository.findByFileNameAndUserId(fileName, user.getId())
                .orElseThrow(() -> new NoResourceFoundException("Файл с именем '" + fileName + "' не найден"));
    }

    private User getUserByLogin(String login) {
        LOGGER.debug("Получение пользователя по логину {}", login);
        return userRepository.findByUsername(login)
                .orElseThrow(() -> {
                    LOGGER.error("Пользователь не найден: {}", login);
                    return new RuntimeException("Пользователь не найден");
                });
    }
}
