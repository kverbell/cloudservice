package ru.netology.cloudservice.Exceptions;

public class NoResourceFoundException extends RuntimeException {
    public NoResourceFoundException(String message) {
        super(message);
    }
}
