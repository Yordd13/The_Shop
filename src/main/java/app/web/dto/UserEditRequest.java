package app.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class UserEditRequest {
    @Pattern(regexp = "^.{2,20}$|^$", message = "First name length must be between 2 and 20 characters!")
    private String firstName;

    @Pattern(regexp = "^.{2,20}$|^$", message = "Last name length must be between 2 and 20 characters!")
    private String lastName;

    @URL
    private String profilePicture;
}
