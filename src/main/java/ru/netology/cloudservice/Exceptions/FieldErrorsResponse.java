package ru.netology.cloudservice.Exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldErrorsResponse {
    private List<String> email;
    private List<String> password;

    public FieldErrorsResponse(List<String> email, List<String> password) {
        this.email = email;
        this.password = password;
    }
}
