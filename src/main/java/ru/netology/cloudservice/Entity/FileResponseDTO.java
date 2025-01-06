package ru.netology.cloudservice.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDTO {
    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("size")
    private double size;
}
