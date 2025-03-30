package app.web;

import app.category.service.CategoryService;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));

    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

    }

    @Test
    void getRequestToLoginPageWithErrorParameter_shouldReturnLoginViewWithErrorMessage() throws Exception {

        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));

    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username","yordd06")
                .formField("email","yordd06@gmail.com")
                .formField("password","123456789")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService,times(1)).register(any());

    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_shouldReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username","aa")
                .formField("email","")
                .formField("password","123456789")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService,never()).register(any());

    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationDetails principal = new AuthenticationDetails(userId, "User123", "123456789", List.of(UserRole.USER), true);
        MockHttpServletRequestBuilder request = get("/categories")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("categoryList"))
                .andExpect(model().attributeExists("cartQuantity"))
                .andExpect(model().attributeExists("activeProductCounts"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/categories");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());

    }

}
