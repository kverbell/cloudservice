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

import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

    public String generateUniqueFileName(String currentFileName, String login) {
        LOGGER.info("Начинаем генерацию уникального имени для файла '{}'", currentFileName);

        User user = getUserByLogin(login);
        LOGGER.debug("Получен пользователь с логином '{}'", login);

        String[] nameParts = currentFileName.split("\\.");
        String baseName = nameParts[0];
        String extension = nameParts.length > 1 ? "." + nameParts[1] : "";

        String[] baseNameParts = baseName.split("_");

        baseName = baseNameParts[0];

        Random random = new Random();
        String newFileName;

        do {
            int randomDigits = 100000 + random.nextInt(900000);
            newFileName = baseName + "_" + randomDigits + extension;

            LOGGER.debug("Проверяем существование файла с именем '{}'", newFileName);
        } while (fileDataRepository.findByFileNameAndUserId(newFileName, user.getId()).isPresent());

        LOGGER.info("Сгенерировано уникальное имя файла: {}", newFileName);
        return newFileName;
    }

    public void updateFileName(String currentFileName, String newFileName, String login) {
        LOGGER.info("Пользователь с логином {} переименовывает файл '{}' на '{}'", login, currentFileName, newFileName);
        User user = getUserByLogin(login);

        FileData fileData = fileDataRepository.findByFileNameAndUserId(currentFileName, user.getId())
                .orElseThrow(() -> new NoResourceFoundException("Файл с именем '" + currentFileName + "' не найден"));

        Optional<FileData> existingFile = fileDataRepository.findByFileNameAndUserId(newFileName, user.getId());

        while (existingFile.isPresent()) {
            LOGGER.error("Файл с именем '{}' уже существует для пользователя '{}'", newFileName, login);
            newFileName = generateUniqueFileName(newFileName, login);
        }

        Path oldFilePath = Paths.get(uploadDir, currentFileName);
        Path newFilePath = Paths.get(uploadDir, newFileName);

        try {
            Files.move(oldFilePath, newFilePath);
            LOGGER.info("Файл '{}' успешно переименован в '{}' на диске", currentFileName, newFileName);
        } catch (IOException e) {
            LOGGER.error("Ошибка при переименовании файла на диске: {}", e.getMessage());
            throw new RuntimeException("Ошибка при переименовании файла на диске");
        }

        fileData.setFileName(newFileName);
        fileData.setFilePath(newFilePath.toString());
        fileDataRepository.save(fileData);

        LOGGER.info("Файл '{}' успешно переименован в '{}' для пользователя '{}'", currentFileName, newFileName, login);
    }

    public List<FileResponseDTO> getAllFiles(String login) {
        User user = getUserByLogin(login);
        List<FileData> fileDataList = fileDataRepository.findAllByUserId(user.getId());

        return fileDataList.stream()
                .map(fileData -> {
                    double fileSizeInMB = fileData.getFileSize();
                    return new FileResponseDTO(fileData.getFileName(), fileSizeInMB);
                })
                .collect(Collectors.toList());
    }

    public void addFile(String fileName, String login, MultipartFile file) throws IOException {
        User user = getUserByLogin(login);
        LOGGER.debug("Пользователь {} загружает файл {}", login, fileName);

        Optional<FileData> existingFileOpt = fileDataRepository.findByFileNameAndUserId(fileName, user.getId());
        if (existingFileOpt.isPresent()) {
            LOGGER.error("Файл с именем {} уже существует для пользователя {}", fileName, login);
            throw new FileAlreadyExistsException("Файл с таким именем уже существует");
        }

        Path path = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        long fileSize = Files.size(path);

        FileData fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFilePath(path.toString());
        fileData.setFileSize(fileSize);
        fileData.setUser(user);

        fileDataRepository.save(fileData);
        LOGGER.info("Файл '{}' размером '{}' успешно загружен пользователем с логином {}", fileName, fileSize, login);
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
