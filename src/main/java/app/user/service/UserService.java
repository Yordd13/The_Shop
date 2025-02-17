package app.user.service;

import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptionalUsername = userRepository.findByUsername(registerRequest.getUsername());

        if(userOptionalUsername.isPresent()) {
            throw new DomainException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        Optional<User> userOptionalEmail = userRepository.findByEmail(registerRequest.getEmail());

        if(userOptionalEmail.isPresent()) {
            throw new DomainException("Email [%s] already exists.".formatted(registerRequest.getEmail()));
        }

        User user = userRepository.save(initilizeUser(registerRequest));


        log.info("Username [{}] has been created.", user.getUsername());



        return user;
    }

    public User login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if(userOptional.isEmpty()) {
            throw new DomainException("Email [%s] is not correct.".formatted(loginRequest.getEmail()));
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            throw new DomainException("Password is not correct.");
        }

        User user = userOptional.get();

        log.info("Username [{}] has logged in.", user.getUsername());

        return user;
    }

    private User initilizeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .profilePictureUrl("https://cdn.pixabay.com/photo/2018/11/13/21/43/avatar-3814049_1280.png")
                .isActive(true)
                .isBannedFromSelling(false)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {



        return null;
    }
}
