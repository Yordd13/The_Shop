package app.category;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.category.service.CategoryService;
import app.products.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void givenValidInputs_whenAddCategory_thenSaveCategory() {

        // Given
        String categoryName = "Electronics";
        String categoryPictureUrl = "https://example.com/electronics.jpg";
        String description = "All kinds of electronics.";

        // When
        categoryService.addCategory(categoryName, categoryPictureUrl, description);

        // Then
        verify(categoryRepository).save(argThat(category ->
                category.getCategoryName().equals(categoryName) &&
                        category.getCategoryPictureUrl().equals(categoryPictureUrl) &&
                        category.getDescription().equals(description)
        ));

    }

    @Test
    void givenCategoriesWithVisibleProducts_whenGetActiveProductsCount_thenReturnCorrectCounts() {

        // Given
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        Category category1 = Category.builder()
                .id(categoryId1)
                .products(
                        List.of(
                                Product.builder().isVisible(true).build(),
                                Product.builder().isVisible(false).build(),
                                Product.builder().isVisible(true).build()
                        ))
                .build();

        Category category2 = Category.builder()
                .id(categoryId2)
                .products(
                        List.of(
                                Product.builder().isVisible(true).build(),
                                Product.builder().isVisible(true).build()
                        ))
                .build();

        when(categoryService.getAllCategories()).thenReturn(List.of(category1, category2));

        // When
        Map<UUID, Integer> result = categoryService.getActiveProductsCount();

        // Then
        assertEquals(2, result.get(categoryId1));
        assertEquals(2, result.get(categoryId2));

    }

    @Test
    void givenCategoriesWithNoProducts_whenGetActiveProductsCount_thenReturnZero() {

        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .products(Collections.emptyList())
                .build();

        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        // When
        Map<UUID, Integer> result = categoryService.getActiveProductsCount();

        // Then
        assertEquals(0, result.get(categoryId));

    }

    @Test
    void givenCategoriesWithOnlyInvisibleProducts_whenGetActiveProductsCount_thenReturnZero() {

        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .products(
                        List.of(
                                Product.builder().isVisible(false).build(),
                                Product.builder().isVisible(false).build()
                        ))
                .build();

        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        // When
        Map<UUID, Integer> result = categoryService.getActiveProductsCount();

        // Then
        assertEquals(0, result.get(categoryId));

    }
}
