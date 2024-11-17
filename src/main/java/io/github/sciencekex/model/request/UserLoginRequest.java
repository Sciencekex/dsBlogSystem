package io.github.sciencekex.model.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
}
