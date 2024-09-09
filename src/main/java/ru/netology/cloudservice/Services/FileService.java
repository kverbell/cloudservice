package ru.netology.cloudservice.Services;

import ru.netology.cloudservice.Entity.FileData;
import ru.netology.cloudservice.Entity.User;
import ru.netology.cloudservice.Repositories.FileDataRepository;
import ru.netology.cloudservice.Repositories.UserRepository;

import org.springframework.stereotype.Service;

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
        User user = getUserById(userId);

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
        FileData fileData = getFileByNameAndUserId(fileName, userId);
        fileDataRepository.delete(fileData);
    }

    public FileData getFileByName(String fileName, Long userId) {
        return getFileByNameAndUserId(fileName, userId);
    }

    private FileData getFileByNameAndUserId(String fileName, Long userId) {
        Optional<FileData> fileDataOpt = fileDataRepository.findByFileNameAndUserId(fileName, userId);
        if (fileDataOpt.isEmpty()) {
            throw new RuntimeException("Файл не найден");
        }
        return fileDataOpt.get();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
