package app.user;

import app.email.service.EmailService;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
import app.products.service.ProductService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private ProductService productService;
    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUser_whenEditTheirUserProfile_thenChangeTheirDetailsAndSaveToDatabase() {

        //Given
        User user = User.builder().build();
        UserEditRequest dto = UserEditRequest
                .builder()
                .firstName("Yordan")
                .lastName("Dimitrov")
                .profilePicture("image.com")
                .build();

        // When
        userService.editUserProfile(user, dto);

        //Then
        assertEquals("Yordan", user.getFirstName());
        assertEquals("Dimitrov", user.getLastName());
        assertEquals("image.com",user.getProfilePictureUrl());

        verify(userRepository,times(1)).save(user);

    }


    @Test
    void givenExistingUserWithoutSellerRole_whenAddSellerRole_thenAddSellerRoleToUser() {

        //Given
        User user = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.USER)))
                .build();

        //When
        userService.addSellerRole(user);

        //Then
        assertTrue(user.getRoles().contains(UserRole.SELLER));
        assertFalse(user.getRoles().contains(UserRole.USER));
        verify(userRepository,times(1)).save(user);

    }

    @Test
    void givenExistingUserWithSellerRole_whenAddSellerRole_thenDontDoAnything() {

        //Given
        User user = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.SELLER)))
                .build();

        //When
        userService.addSellerRole(user);

        //Then
        assertTrue(user.getRoles().contains(UserRole.SELLER));

        verify(userRepository,times(0)).save(user);

    }

    @Test
    void givenUserWithOrderedItems_whenGetOrderQuantity_thenReturnCorrectSum() {

        // Given
        User user = User.builder().build();
        OrderItem item1 = OrderItem
                .builder()
                .quantity(1)
                .build();
        OrderItem item2 = OrderItem
                .builder()
                .quantity(7)
                .build();
        user.setOrderItems(List.of(item1, item2));

        // When
        int result = userService.getOrderQuantity(user);

        // Then
        assertEquals(8, result);

    }

    @Test
    void givenUserWithOrderItemsThatHaveOrders_whenGetOrderQuantity_thenReturnZero() {

        // Given
        User user = User.builder().build();
        Order order = Order.builder().build();
        OrderItem item1 = OrderItem
                .builder()
                .quantity(1)
                .order(order)
                .build();
        OrderItem item2 = OrderItem
                .builder()
                .quantity(7)
                .order(order)
                .build();
        user.setOrderItems(List.of(item1, item2));

        // When
        int result = userService.getOrderQuantity(user);

        // Then
        assertEquals(0, result);

    }

    @Test
    void givenUserWithEmptyOrderItemsList_whenGetOrderQuantity_thenReturnZero() {

        // Given
        User user = User.builder().build();
        user.setOrderItems(new ArrayList<>());

        // When
        int result = userService.getOrderQuantity(user);

        // Then
        assertEquals(0, result);

    }

    @Test
    void givenExistingUserWithoutAdminRole_whenAddAdminRole_thenAddAdminRoleToUser() {

        //Given
        User user = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.USER)))
                .build();

        //When
        userService.addAdminRole(user);

        //Then
        assertTrue(user.getRoles().contains(UserRole.ADMIN));
        assertFalse(user.getRoles().contains(UserRole.USER));
        verify(userRepository,times(1)).save(user);

    }

    @Test
    void givenExistingUserWithAdminRole_whenAddAdminRole_thenDontDoAnything() {

        //Given
        User user = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .build();

        //When
        userService.addAdminRole(user);

        //Then
        assertTrue(user.getRoles().contains(UserRole.ADMIN));

        verify(userRepository,times(0)).save(user);

    }

    @Test
    void givenExistingUser_whenChangeStatus_thenChangeHisStatus() {

        //Given
        User user = User
                .builder()
                .isActive(true)
                .build();

        //When
        userService.changeStatus(user);

        //Then
        assertFalse(user.isActive());

        verify(userRepository,times(1)).save(user);

    }
}
