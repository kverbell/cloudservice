package ru.netology.cloudservice.Services;

import org.springframework.stereotype.Service;
import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Repositories.FileDataRepository;
import ru.netology.cloudservice.Repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileDataRepository fileDataRepository;
    private final UserRepository userRepository;

    public FileService(FileDataRepository fileDataRepository, UserRepository userRepository) {
        this.fileDataRepository = fileDataRepository;
        this.userRepository = userRepository;
    }

    public List<FileData> getAllFiles(Long userId) {
        return fileDataRepository.findAllByUserId(userId);
    }

    public void addFile(String fileName, byte[] fileContent, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }
        User user = userOpt.get();

        Optional<FileData> existingFileOpt = fileDataRepository.findByFileNameAndUserId(fileName, userId);
        if (existingFileOpt.isPresent()) {
            throw new RuntimeException("Файл с таким именем уже существует");
        }

        FileData fileData = new FileData();
        fileData.setFileName(fileName);
        fileData.setFileContent(fileContent);
        fileData.setUser(user);
        fileDataRepository.save(fileData);
    }

    public void deleteFile(String fileName, Long userId) {
        Optional<FileData> fileDataOpt = fileDataRepository.findByFileName(fileName);
        if (fileDataOpt.isEmpty()) {
            throw new RuntimeException("Файл не найден");
        }
        FileData fileData = fileDataOpt.get();
        if (!fileData.getUser().getId().equals(userId)) {
            throw new RuntimeException("Для удаления файла нужно авторизоваться");
        }
        fileDataRepository.delete(fileData);
    }

    public FileData getFileByName(String fileName, Long userId) {
        Optional<FileData> fileDataOpt = fileDataRepository.findByFileName(fileName);
        if (fileDataOpt.isEmpty()) {
            throw new RuntimeException("Файл не найден");
        }
        FileData fileData = fileDataOpt.get();
        if (!fileData.getUser().getId().equals(userId)) {
            throw new RuntimeException("Для получения файла нужно авторизоваться");
        }
        return fileData;
    }
}
