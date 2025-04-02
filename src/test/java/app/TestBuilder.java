package app;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.products.model.Product;
import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestBuilder {
    public static User aRandomUser() {

        return User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123456789")
                .roles(List.of(UserRole.USER))
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static Product aRandomProduct() {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Product Name")
                .description("Product Description!")
                .isVisible(true)
                .isRemoved(false)
                .seller(aRandomUser())
                .updatedOn(LocalDateTime.now())
                .quantity(100)
                .category(Category.builder()
                        .id(UUID.randomUUID())
                        .categoryName("Category Name")
                        .description("Category Description!")
                        .categoryPictureUrl("categoryImage.com")
                        .build())
                .price(BigDecimal.TEN)
                .image("image.com")
                .build();
    }
}
