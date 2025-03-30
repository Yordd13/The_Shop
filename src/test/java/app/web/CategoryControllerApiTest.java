package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.products.model.Product;
import app.products.service.ProductService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
public class CategoryControllerApiTest {

    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategoryPageView_authenticatedUser_withoutSearch() throws Exception {

        String categoryName = "ELECTRONICS";
        User mockUser = aRandomUser();
        Product product = Product.builder()
                .isVisible(true)
                .build();
        Category mockCategory = Category
                .builder()
                .categoryName(categoryName)
                .products(List.of(product))
                .build();
        int cartQuantity = 2;

        UUID userId = UUID.randomUUID();
        AuthenticationDetails principal = new AuthenticationDetails(userId, "User123", "123456789", List.of(UserRole.USER), true);

        when(userService.getById(userId)).thenReturn(mockUser);
        when(userService.getOrderQuantity(mockUser)).thenReturn(cartQuantity);
        when(categoryService.getByName(categoryName.toUpperCase())).thenReturn(mockCategory);

        MockHttpServletRequestBuilder request = get("/categories/" + categoryName.toLowerCase())
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("cartQuantity"))
                .andExpect(model().attribute("category", mockCategory))
                .andExpect(model().attribute("cartQuantity", cartQuantity));

        verify(userService, times(1)).getById(userId);
        verify(categoryService, times(1)).getByName(categoryName.toUpperCase());

    }

    @Test
    void getCategoryPageView_authenticatedUser_withSearch() throws Exception {

        String categoryName = "ELECTRONICS";
        String searchQuery = "Laptop";
        User mockUser = aRandomUser();
        Product product = Product.builder()
                .isVisible(true)
                .build();
        Category mockCategory = Category
                .builder()
                .categoryName(categoryName)
                .products(List.of(product))
                .build();
        int cartQuantity = 2;

        UUID userId = UUID.randomUUID();
        AuthenticationDetails principal = new AuthenticationDetails(userId, "User123", "123456789", List.of(UserRole.USER), true);

        when(userService.getById(userId)).thenReturn(mockUser);
        when(userService.getOrderQuantity(mockUser)).thenReturn(cartQuantity);
        when(categoryService.getByName(categoryName.toUpperCase())).thenReturn(mockCategory);
        when(productService.searchProductsByCategory(mockCategory, searchQuery)).thenReturn(mockCategory.getProducts());

        MockHttpServletRequestBuilder request = get("/categories/" + categoryName.toLowerCase())
                .param("search", searchQuery)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("category"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("cartQuantity"))
                .andExpect(model().attribute("category", mockCategory))
                .andExpect(model().attribute("cartQuantity", cartQuantity));

        verify(userService, times(1)).getById(userId);
        verify(categoryService, times(1)).getByName(categoryName.toUpperCase());
        verify(productService, times(1)).searchProductsByCategory(mockCategory, searchQuery);

    }
}
