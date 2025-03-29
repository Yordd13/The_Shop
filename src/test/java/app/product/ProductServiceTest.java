package app.product;

import app.category.model.Category;
import app.exception.DomainException;
import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.products.repository.ProductRepository;
import app.products.service.ProductService;
import app.user.model.User;
import app.web.dto.NewProductRequest;
import app.web.dto.UpdateQuantityRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenUserThatIsBannedFromSelling_whenAddNewProduct_thenThrowsException() {

        //Given
        User user = User.builder()
                .isBannedFromSelling(true)
                .build();
        NewProductRequest newProductRequest = NewProductRequest.builder().build();

        //When and Then
        assertThrows(DomainException.class,()->productService.addNewProduct(newProductRequest,user));

    }

    @Test
    void givenUserThatIsNotBannedFromSelling_whenAddNewProduct_thenAddTheNewProductToTheDatabase() {

        //Given
        User user = User.builder()
                .isBannedFromSelling(false)
                .build();
        NewProductRequest newProductRequest = NewProductRequest
                .builder()
                .description("Testing the description!")
                .category(Category.builder().build())
                .name("Test")
                .image("image.com")
                .price(BigDecimal.valueOf(100))
                .quantity(10)
                .build();

        //When
        productService.addNewProduct(newProductRequest,user);

        //Then
        verify(productRepository).save(argThat(product ->
                product.getName().equals(newProductRequest.getName()) &&
                        product.getPrice().equals(newProductRequest.getPrice()) &&
                        product.getQuantity() == newProductRequest.getQuantity() &&
                        product.getDescription().equals(newProductRequest.getDescription()) &&
                        product.getImage().equals(newProductRequest.getImage()) &&
                        product.getCategory().equals(newProductRequest.getCategory())
        ));

    }

    @Test
    void givenUserWithProducts_whenDeleteProductsByUser_thenSetProductsInvisibleAndSave() {

        // Given
        User user = User.builder().build();
        Product product1 = Product.builder()
                .isVisible(true)
                .build();
        Product product2 = Product.builder()
                .isVisible(true)
                .build();

        when(productRepository.getProductsBySeller(user)).thenReturn(List.of(product1, product2));

        // When
        productService.deleteProductsByUser(user);

        // Then
        assertFalse(product1.isVisible());
        assertFalse(product2.isVisible());

        verify(productRepository).getProductsBySeller(user);
        verify(productRepository, times(2)).save(any(Product.class));

    }

    @Test
    void givenUserWithNoProducts_whenDeleteProductsByUser_thenDoNothing() {

        // Given
        User user = User.builder().build();

        when(productRepository.getProductsBySeller(user)).thenReturn(List.of());

        // When
        productService.deleteProductsByUser(user);

        // Then
        verify(productRepository).getProductsBySeller(user);
        verify(productRepository, never()).save(any());

    }

    @Test
    void givenEligibleProducts_whenSetProductsToBeVisibleAgain_thenMakeThemVisibleAndReturnTrue() {

        // Given
        User user = User.builder().build();
        Product product1 = Product.builder()
                .isRemoved(false)
                .quantity(5)
                .isVisible(false)
                .build();
        Product product2 = Product.builder()
                .isRemoved(false)
                .quantity(2)
                .isVisible(false)
                .build();

        when(productRepository.getProductsBySeller(user)).thenReturn(List.of(product1, product2));

        // When
        boolean result = productService.setProductsToBeVisibleAgain(user);

        // Then
        assertTrue(result);
        assertTrue(product1.isVisible());
        assertTrue(product2.isVisible());

        verify(productRepository).getProductsBySeller(user);
        verify(productRepository, times(2)).save(any(Product.class));

    }

    @Test
    void givenNoEligibleProducts_whenSetProductsToBeVisibleAgain_thenReturnFalse() {

        // Given
        User user = User.builder().build();
        Product product1 = Product.builder()
                .isRemoved(true)
                .quantity(5)
                .isVisible(false)
                .build();
        Product product2 = Product.builder()
                .isRemoved(false)
                .quantity(0)
                .isVisible(false)
                .build();

        when(productRepository.getProductsBySeller(user)).thenReturn(List.of(product1, product2));

        // When
        boolean result = productService.setProductsToBeVisibleAgain(user);

        // Then
        assertFalse(result);
        assertFalse(product1.isVisible());
        assertFalse(product2.isVisible());

        verify(productRepository).getProductsBySeller(user);
        verify(productRepository, times(0)).save(any(Product.class));

    }

    @Test
    void givenUserWithNoProducts_whenSetProductsToBeVisibleAgain_thenReturnFalse() {

        // Given
        User user = User.builder().build();

        when(productRepository.getProductsBySeller(user)).thenReturn(List.of());

        // When
        boolean result = productService.setProductsToBeVisibleAgain(user);

        // Then
        assertFalse(result);
        verify(productRepository).getProductsBySeller(user);
        verify(productRepository, never()).save(any());

    }

    @Test
    void givenProduct_whenHideProduct_thenSetProductToNotBeVisible() {

        //Given
        Product product = Product.builder()
                .isVisible(true)
                .build();

        //When
        productService.hideProduct(product);

        //Then
        assertFalse(product.isVisible());
        verify(productRepository, times(1)).save(product);

    }

    @Test
    void givenValidUpdateQuantityRequest_whenUpdateQuantity_thenIncreaseQuantityAndSetVisible() {

        // Given
        Product product = Product.builder()
                .quantity(5)
                .isVisible(false)
                .build();
        UpdateQuantityRequest request = UpdateQuantityRequest.builder()
                .quantity(10)
                .build();

        // When
        productService.updateQuantity(product, request);

        // Then
        assertEquals(15, product.getQuantity());
        assertTrue(product.isVisible());

        verify(productRepository).save(product);

    }
    @Test
    void givenProductNotInActiveOrder_whenRemoveProduct_thenDeleteProduct() {

        // Given
        Product product = Product.builder()
                .isRemoved(false)
                .isVisible(true)
                .build();
        when(orderItemService.containsProductWithOrderNotNull(product)).thenReturn(false);

        // When
        productService.removeProduct(product);

        // Then
        verify(productRepository).delete(product);
        verify(productRepository, never()).save(any());

    }

    @Test
    void givenProductInActiveOrder_whenRemoveProduct_thenMarkAsRemovedAndInvisible() {

        // Given
        Product product = Product.builder()
                .isRemoved(false)
                .isVisible(true)
                .build();
        when(orderItemService.containsProductWithOrderNotNull(product)).thenReturn(true);

        // When
        productService.removeProduct(product);

        // Then
        assertTrue(product.isRemoved());
        assertFalse(product.isVisible());

        verify(productRepository, never()).delete(any());
        verify(productRepository).save(product);

    }
}
