package ru.netology.cloudservice.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileResponseDTO {
    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("size")
    private long size;

    public FileResponseDTO(String filename, long size) {
        this.fileName = filename;
        this.size = size;
    }
}
