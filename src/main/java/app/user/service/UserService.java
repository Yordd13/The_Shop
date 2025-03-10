package app.user.service;

import app.orderItem.model.OrderItem;
import app.security.AuthenticationDetails;
import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void register(RegisterRequest registerRequest) {

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

    }


    private User initilizeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(List.of(UserRole.USER))
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

        return new AuthenticationDetails(user.getId(),email,user.getPassword(),user.getRoles(),user.isActive());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new DomainException("User with this id not found!"));
    }

    public void editUserProfile(User user, UserEditRequest userEditRequest) {
        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setProfilePictureUrl(userEditRequest.getProfilePicture());
        user.setUpdatedOn(LocalDateTime.now());

        userRepository.save(user);
    }

    public void addSellerRole(User user) {
        if(!user.getRoles().contains(UserRole.SELLER)){
            user.getRoles().add(UserRole.SELLER);
            userRepository.save(user);
        }
    }

    public int getOrderQuantity(User user) {
        AtomicInteger count = new AtomicInteger();
        List<OrderItem> orderItems = user.getOrderItems().stream()
                .filter(orderItem -> orderItem.getOrder() == null)
                .toList();

        orderItems.forEach(orderItem -> {
            count.addAndGet(orderItem.getQuantity());
        });

        return count.get();
    }

}
