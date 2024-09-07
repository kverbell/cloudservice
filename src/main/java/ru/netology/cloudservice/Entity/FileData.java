package ru.netology.cloudservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "file_data")
public class FileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

}
