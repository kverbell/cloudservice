package ru.netology.cloudservice.Exceptions;

import lombok.Getter;

@Getter
public class InvalidLoginOrPasswordException extends RuntimeException {
    private final String errorField;

    public InvalidLoginOrPasswordException(String message, String errorField) {
        super(message);
        this.errorField = errorField;
    }

}

