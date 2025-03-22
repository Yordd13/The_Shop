package app.user.service;

import app.email.service.EmailService;
import app.exception.RegistrationException;
import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, ProductService productService, OrderItemService orderItemService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {

        List<String> errorMessages = new ArrayList<>();

        Optional<User> userOptionalUsername = userRepository.findByUsername(registerRequest.getUsername());
        if (userOptionalUsername.isPresent()) {
            errorMessages.add("Username " + registerRequest.getUsername() + " already exists.");
        }

        Optional<User> userOptionalEmail = userRepository.findByEmail(registerRequest.getEmail());
        if (userOptionalEmail.isPresent()) {
            errorMessages.add("Email " + registerRequest.getEmail() + " already exists.");
        }

        if (!errorMessages.isEmpty()) {
            throw new RegistrationException(errorMessages);
        }

        User user = userRepository.save(initilizeUser(registerRequest));

        emailService.saveNotificationPreference(user.getId(),true,user.getEmail());

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
            user.getRoles().remove(UserRole.USER);
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addAdminRole(User user) {
        if(!user.getRoles().contains(UserRole.ADMIN)){
            user.getRoles().add(UserRole.ADMIN);
            user.getRoles().remove(UserRole.USER);
            userRepository.save(user);
        }
    }

    public void changeStatus(User user) {
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void changeBanFromSelling(User user) {
        user.setBannedFromSelling(!user.isBannedFromSelling());

        if(user.isBannedFromSelling()){
            productService.deleteProductsByUser(user);
            orderItemService.deleteOrderItemsByUserThatAreNull(user);
        }
        if(user.getRoles().contains(UserRole.SELLER)){
            user.getRoles().remove(UserRole.SELLER);
        }

        userRepository.save(user);
    }

    public void changeRole(User user) {
        if(user.getRoles().contains(UserRole.ADMIN)){
            user.getRoles().remove(UserRole.ADMIN);
            user.getRoles().add(UserRole.USER);
        } else {
            user.getRoles().remove(UserRole.USER);
            user.getRoles().add(UserRole.ADMIN);
        }

        userRepository.save(user);
    }
}
