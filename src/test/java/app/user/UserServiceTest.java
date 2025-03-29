package app.user;

import app.email.service.EmailService;
import app.exception.DomainException;
import app.exception.RegistrationException;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    void givenExistingUserWithActiveStatus_whenChangeStatus_thenChangeHisStatusToInactive() {

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
    @Test
    void givenExistingUserWithInactiveStatus_whenChangeStatus_thenChangeHisStatusToActive() {

        //Given
        User user = User
                .builder()
                .isActive(false)
                .build();

        //When
        userService.changeStatus(user);

        //Then
        assertTrue(user.isActive());

        verify(userRepository,times(1)).save(user);

    }

    @Test
    void givenExistingUserWithRoleAdmin_whenChangeRoleOfAnotherUserFromAdminToUser_thenChangeHisRoleToUser() {

        //Given
        User adminUser = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .username("admin")
                .build();
        User currentUser = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .username("current")
                .build();

        //When
        boolean result = userService.changeRole(adminUser, currentUser);

        //Then
        assertFalse(result);
        assertFalse(adminUser.getRoles().contains(UserRole.ADMIN));
        assertTrue(adminUser.getRoles().contains(UserRole.USER));

        verify(userRepository, times(1)).save(adminUser);
    }

    @Test
    void givenExistingUserWithRoleAdmin_whenChangeRoleOfAnotherUserFromUserToAdmin_thenChangeHisRoleToUser() {

        //Given
        User normalUser = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.USER)))
                .username("admin")
                .build();
        User currentUser = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .username("current")
                .build();

        //Then
        boolean result = userService.changeRole(normalUser, currentUser);

        //Given
        assertFalse(result);
        assertFalse(normalUser.getRoles().contains(UserRole.USER));
        assertTrue(normalUser.getRoles().contains(UserRole.ADMIN));

        verify(userRepository, times(1)).save(normalUser);
    }

    @Test
    void givenExistingUserWithRoleAdmin_whenChangeHisPersonalRoleFromAdminToUser_thenChangeHisPersonalRoleToUserAndClearTheContext() {

        //Given
        User currentUser = User
                .builder()
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .username("current")
                .build();

        //When
        boolean result = userService.changeRole(currentUser, currentUser);

        //Then
        assertTrue(result);
        assertFalse(currentUser.getRoles().contains(UserRole.ADMIN));
        assertTrue(currentUser.getRoles().contains(UserRole.USER));

        verify(userRepository, times(1)).save(currentUser);

    }

    @Test
    void givenUserWithExistingUsernameAndExistingEmail_whenRegister_thenThrowException() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .email("email@gmail.com")
                .password("123456789")
                .build();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(User.builder().build()));

        //When and Then
        assertThrows(RegistrationException.class, () -> userService.register(registerRequest));

        verify(userRepository,never()).save(any());
        verify(emailService,never()).saveNotificationPreference(any(UUID.class),anyBoolean(),anyString());

    }

    @Test
    void givenUserWithExistingUsernameAndUniqueEmail_whenRegister_thenThrowException() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .email("email@gmail.com")
                .password("123456789")
                .build();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        //When and Then
        assertThrows(RegistrationException.class, () -> userService.register(registerRequest));

        verify(userRepository,never()).save(any());
        verify(emailService,never()).saveNotificationPreference(any(UUID.class),anyBoolean(),anyString());

    }

    @Test
    void givenUserWithUniqueUsernameAndExistingEmail_whenRegister_thenThrowException() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .email("email@gmail.com")
                .password("123456789")
                .build();
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(User.builder().build()));

        //When and Then
        assertThrows(RegistrationException.class, () -> userService.register(registerRequest));

        verify(userRepository,never()).save(any());
        verify(emailService,never()).saveNotificationPreference(any(UUID.class),anyBoolean(),anyString());

    }

    @Test
    void givenUserWithUniqueUsernameAndUniqueEmail_whenRegister_thenRegisterSuccessfully() {

        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .email("email@gmail.com")
                .password("123456789")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .build();

        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When and Then
        assertDoesNotThrow(() -> userService.register(registerRequest));

        verify(userRepository).save(any(User.class));
        verify(emailService).saveNotificationPreference(eq(user.getId()), eq(true), eq(registerRequest.getEmail()));
    }


    @Test
    void givenExistingUser_whenLoadUserByEmail_thenThrowException() {

        //Given
        String email = "email@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //When and Then
        assertThrows(DomainException.class, () -> userService.loadUserByUsername(email));
    }

    @Test
    void givenExistingUser_whenLoadUserByEmail_thenReturnCorrectAuthenticationMetadata(){

        //Given
        String email = "email@gmail.com";
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password("123456789")
                .roles(new ArrayList<>(List.of(UserRole.ADMIN)))
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //When
        UserDetails userDetails = userService.loadUserByUsername(email);

        //Then
        assertInstanceOf(AuthenticationDetails.class, userDetails);
        AuthenticationDetails result = (AuthenticationDetails) userDetails;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(email, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isActive());
        assertTrue(user.getRoles().contains(UserRole.ADMIN));
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void givenExistingUserThatIsNotBannedFromSelling_whenChangeBanFromSelling_thenBanHimFromSellingAndHideHisProductsAndDeleteOrderItemsAndRemoveSellerRole(){

        //Given
        User user = User.builder()
                .isBannedFromSelling(false)
                .roles(new ArrayList<>(List.of(UserRole.SELLER)))
                .build();

        //When
        userService.changeBanFromSelling(user);

        //Then
        assertTrue(user.isBannedFromSelling());
        assertFalse(user.getRoles().contains(UserRole.SELLER));

        verify(productService).deleteProductsByUser(user);
        verify(orderItemService).deleteOrderItemsByUserThatAreNull(user);
        verify(userRepository).save(user);
    }

    @Test
    void givenExistingUserThatIsBannedFromSelling_whenChangeBanFromSelling_thenUnbanHimAndReassignSellerRoleAndShowHisProductsIfHeHas() {

        // Given
        User user = User.builder()
                .isBannedFromSelling(true)
                .roles(new ArrayList<>(List.of(UserRole.USER)))
                .build();

        when(productService.setProductsToBeVisibleAgain(user)).thenReturn(true);

        // When
        userService.changeBanFromSelling(user);

        // Then
        assertFalse(user.isBannedFromSelling());
        assertTrue(user.getRoles().contains(UserRole.SELLER));

        verify(productService).setProductsToBeVisibleAgain(user);
        verify(userRepository).save(user);
    }

    @Test
    void givenExistingUserThatIsBannedFromSelling_whenChangeBanFromSelling_thenUnbanHimWithoutReassigningSellerRoleAndHeHasNoProducts() {

        // Given
        User user = User.builder()
                .isBannedFromSelling(true)
                .roles(new ArrayList<>(List.of(UserRole.USER)))
                .build();

        when(productService.setProductsToBeVisibleAgain(user)).thenReturn(false);

        // When
        userService.changeBanFromSelling(user);

        // Then
        assertFalse(user.isBannedFromSelling());
        assertFalse(user.getRoles().contains(UserRole.SELLER));

        verify(productService).setProductsToBeVisibleAgain(user);
        verify(userRepository).save(user);
    }
}
