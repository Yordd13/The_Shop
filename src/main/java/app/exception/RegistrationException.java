package app.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class RegistrationException extends RuntimeException {
    private final List<String> errorMessages;

    public RegistrationException(List<String> errorMessages) {
        super(String.join(", ", errorMessages));
        this.errorMessages = errorMessages;
    }

}
