package ru.netology.cloudservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file_data")
@Getter
@Setter
public class FileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Lob
    @Column(name = "file_content", nullable = false)
    private byte[] fileContent;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
