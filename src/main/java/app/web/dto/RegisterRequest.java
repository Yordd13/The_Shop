package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email
    private String email;

    @Size(min = 8, message = "Password should be at least 8 symbols long!")
    private String password;

    @Size(min = 6, message = "Username should be at least 6 symbols long!")
    private String username;
}

