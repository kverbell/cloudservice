package ru.netology.cloudservice.Repositories;

import ru.netology.cloudservice.Entity.FileData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileDataRepository extends JpaRepository<FileData, Long> {
    Optional<FileData> findByFileName(String filename);
    Optional<FileData> findByFileNameAndUserId(String filename, Long userId);
    List<FileData> findAllByUserId(Long userId);
}
