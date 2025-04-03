package app.web;

import app.email.client.dto.NotificationPreference;
import app.email.service.EmailService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProfilePage_shouldReturnUserProfileView() throws Exception {
        User user = aRandomUser();
        user.setOrders(List.of());
        when(userService.getByUsername(user.getUsername())).thenReturn(user);
        when(userService.getOrderQuantity(any())).thenReturn(3);

        NotificationPreference notificationPreference = mock(NotificationPreference.class);
        when(notificationPreference.isEnabled()).thenReturn(true);

        when(emailService.getNotificationPreference(user.getId())).thenReturn(notificationPreference);

        MockHttpServletRequestBuilder request = get("/users/profile/" + user.getUsername())
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(), true)))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("cartQuantity"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("isNotificationEnabled"));
    }


    @Test
    void updateNotifications_shouldRedirectToProfile() throws Exception {
        User user = aRandomUser();
        when(userService.getById(any())).thenReturn(user);

        NotificationPreference notificationPreference = mock(NotificationPreference.class);
        when(notificationPreference.isEnabled()).thenReturn(true);

        when(emailService.getNotificationPreference(any())).thenReturn(notificationPreference);

        doNothing().when(emailService).saveNotificationPreference(any(), anyBoolean(), any());

        MockHttpServletRequestBuilder request = get("/users/notifications")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(), true)))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile/" + user.getUsername()));

        verify(emailService, times(1)).saveNotificationPreference(any(), anyBoolean(), any());
    }

    @Test
    void changeRole_shouldRedirectToAdminDashboard() throws Exception {

        User user = aRandomUser();
        User currentUser = new User();
        currentUser.setUsername("adminUser");

        when(userService.getByUsername(user.getUsername())).thenReturn(user);
        when(userService.getById(currentUser.getId())).thenReturn(currentUser);

        when(userService.changeRole(any(), any())).thenReturn(true);

        MockHttpServletRequestBuilder request = get("/users/toggle-role/"+user.getUsername())
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "AdminUser", "password", List.of(UserRole.ADMIN), true)))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(userService, times(1)).changeRole(any(), any());
    }
}
