package app.product;

import app.category.model.Category;
import app.products.service.ProductInit;
import app.products.service.ProductService;
import app.category.service.CategoryService;
import app.user.service.UserService;
import app.user.model.User;
import app.web.dto.NewProductRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;

@SpringBootTest
class ProductInitTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductInit productInit;

    @Test
    void testProductInit_whenTheProgramRunsAndDBisEmpty_thenProductsAreAddedToTheDatabase() throws Exception {

        // Given
        User seller = new User();
        seller.setUsername("admin123");
        when(userService.getByUsername("admin123")).thenReturn(seller);
        when(categoryService.getByName("ELECTRONICS")).thenReturn(new Category());
        when(categoryService.getByName("AUTOMOTIVE")).thenReturn(new Category());
        when(categoryService.getByName("BOOKS")).thenReturn(new Category());
        when(categoryService.getByName("FASHION")).thenReturn(new Category());
        when(categoryService.getByName("TOYS")).thenReturn(new Category());

        // When
        productInit.run();

        // Then
        verify(productService, times(7)).addNewProduct(any(NewProductRequest.class), eq(seller));
        verify(userService, times(1)).addSellerRole(seller);
    }
}

