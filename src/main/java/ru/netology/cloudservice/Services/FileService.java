package ru.netology.cloudservice.Services;

import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Exceptions.FileAlreadyExistsException;
import ru.netology.cloudservice.Exceptions.FileNotFoundException;
import ru.netology.cloudservice.Repositories.FileDataRepository;
import ru.netology.cloudservice.Repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final FileDataRepository fileDataRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FileService(FileDataRepository fileDataRepository, UserRepository userRepository) {
        this.fileDataRepository = fileDataRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
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

    private User getUserByLogin(String login) {
        return userRepository.findByUsername(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public List<FileData> getAllFiles(String login) {
        LOGGER.info("Пользователь с логином {} запрашивает список всех файлов", login);
        User user = getUserByLogin(login);
        List<FileData> files = fileDataRepository.findAllByUserId(user.getId());
        LOGGER.info("Пользователь с логином {} получил {} файлов", login, files.size());
        return files;
    }

    public void addFile(String fileName, byte[] fileContent, String login) {
        User user = getUserByLogin(login);

        Optional<FileData> existingFileOpt = fileDataRepository.findByFileNameAndUserId(fileName, user.getId());
        if (existingFileOpt.isPresent()) {
            throw new FileAlreadyExistsException("Файл с таким именем уже существует");
        }

        FileData fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFileContent(fileContent);
        fileData.setUser(user);
        fileDataRepository.save(fileData);
        LOGGER.info("Файл '{}' успешно загружен пользователем с логином {}", fileName, login);
    }

    public void deleteFile(String fileName, String login) {
        FileData fileData = getFileByNameAndUserLogin(fileName, login);
        fileDataRepository.delete(fileData);
        LOGGER.info("Файл '{}' успешно удален пользователем с логином {}", fileName, login);
    }

    public FileData getFileByNameAndUserLogin(String fileName, String login) {
        LOGGER.info("Пользователь с логином {} запрашивает файл '{}'", login, fileName);
        User user = getUserByLogin(login);
        return fileDataRepository.findByFileNameAndUserId(fileName, user.getId())
                .orElseThrow(() -> new FileNotFoundException("Файл не найден"));
    }

}
