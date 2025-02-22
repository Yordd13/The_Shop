package app.user.service;

import app.security.AuthenticationDetails;
import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        return userRepository.findById(id).orElseThrow(() ->
                new DomainException("User with this id not found!"));
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new DomainException("User with this username doesn't exist."));

        return new AuthenticationDetails(user.getId(),email,user.getPassword(),user.getRole(),user.isActive());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new DomainException("User with this id not found!"));
    }
}
