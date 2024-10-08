package ru.netology.cloudservice.Controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class LoginRequest {
    private String login;
    private String password;
}
