package app;

import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {
    public static User aRandomUser() {

        return User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123456789")
                .roles(List.of(UserRole.USER))
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
