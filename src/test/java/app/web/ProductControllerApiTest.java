package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.orderItem.service.OrderItemService;
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

import static app.TestBuilder.aRandomProduct;
import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private OrderItemService orderItemService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProductPage_shouldReturnProductView() throws Exception {
        User user = aRandomUser();
        Product product = aRandomProduct();
        when(userService.getById(any())).thenReturn(user);
        when(productService.getById(any())).thenReturn(product);
        when(userService.getOrderQuantity(any())).thenReturn(1);

        UUID productId = UUID.randomUUID();
        AuthenticationDetails principal = new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(UserRole.USER), true);

        MockHttpServletRequestBuilder request = get("/products/" + productId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("cartQuantity"));
    }

    @Test
    void postProduct_shouldRedirectToProductPage() throws Exception {
        UUID productId = UUID.randomUUID();
        User user = aRandomUser();
        Product product = aRandomProduct();
        when(userService.getById(any())).thenReturn(user);
        when(productService.getById(any())).thenReturn(product);
        doNothing().when(orderItemService).addNewOrderItem(any(), any());

        MockHttpServletRequestBuilder request = post("/products/" + productId + "/add")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(UserRole.USER), true)))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/" + productId));

        verify(orderItemService,times(1)).addNewOrderItem(any(), any());
    }

    @Test
    void getAddProductPage_shouldReturnNewProductView() throws Exception {
        User user = aRandomUser();
        List<Category> categories = List.of(new Category());
        int cartQuantity = 3;

        when(userService.getById(any())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(userService.getOrderQuantity(any())).thenReturn(cartQuantity);

        MockHttpServletRequestBuilder request = get("/products/add")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(), true)))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-product"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("newProductRequest"))
                .andExpect(model().attributeExists("cartQuantity"));
    }

    @Test
    void addNewProduct_shouldRedirectToCategoriesOnSuccess() throws Exception {
        User user = aRandomUser();
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setCategoryName("ELECTRONICS");

        when(userService.getById(any())).thenReturn(user);
        doNothing().when(productService).addNewProduct(any(), any());
        doNothing().when(userService).addSellerRole(any());

        MockHttpServletRequestBuilder request = post("/products")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(), true)))
                .param("name", "keyboard")
                .param("description", "description description description description description")
                .param("price", "10")
                .param("quantity", "3")
                .param("category.id", category.getId().toString())
                .param("category.categoryName", category.getCategoryName())
                .param("image", "http://example.com/image.jpg")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(productService, times(1)).addNewProduct(any(), any());
        verify(userService, times(1)).addSellerRole(any());
    }
    @Test
    void postNewProduct_shouldReturnNewProductViewOnValidationErrors() throws Exception {
        User user = aRandomUser();
        List<Category> categories = List.of(Category.builder().categoryName("ELECTRONICS").id(UUID.randomUUID()).build());
        int cartQuantity = 3;

        when(userService.getById(any())).thenReturn(user);
        when(categoryService.getAllCategories()).thenReturn(categories);
        when(userService.getOrderQuantity(any())).thenReturn(cartQuantity);

        MockHttpServletRequestBuilder request = post("/products")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(), true)))
                .param("name", "K")
                .param("description", "Short")
                .param("price", "0")
                .param("quantity", "-5")
                .param("category.id", categories.get(0).getId().toString())
                .param("category.categoryName", categories.get(0).getCategoryName())
                .param("image", "invalid-url")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-product"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("newProductRequest"))
                .andExpect(model().attributeExists("cartQuantity"))
                .andExpect(model().attributeHasErrors("newProductRequest"))
                .andExpect(model().attributeHasFieldErrors("newProductRequest", "name", "description", "price", "quantity", "image"));

        verify(productService, never()).addNewProduct(any(), any());
        verify(userService, never()).addSellerRole(any());
    }

}

