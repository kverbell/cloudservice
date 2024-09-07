package ru.netology.cloudservice.Repositories;

import ru.netology.cloudservice.Entity.FileData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileDataRepository extends JpaRepository<FileData, Long> {
    Optional<FileData> findByFilename(String filename);
}
