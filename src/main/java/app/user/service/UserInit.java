package app.user.service;

import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserInit implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public UserInit(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(!userService.getAllUsers().isEmpty()){
            return;
        }

        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .email("admin123@gmail.com")
                .password("123456789")
                .username("admin123")
                .build();

        userService.register(registerRequest);
        userService.addAdminRole(userService.getByUsername("admin123"));
    }
}
