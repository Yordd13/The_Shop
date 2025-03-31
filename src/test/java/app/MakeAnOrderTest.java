package app;

import app.category.service.CategoryService;
import app.email.service.EmailService;
import app.order.model.Order;
import app.order.service.OrderService;
import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.products.service.ProductService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewProductRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MakeAnOrderTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private EmailService emailService;

    @Test
    void userMakesAnOrder_happyPath() {

        //Given
        RegisterRequest registerRequest1 = RegisterRequest.builder()
                .email(UUID.randomUUID() + "@gmail.com")
                .username("test1")
                .password("123456789")
                .build();
        userService.register(registerRequest1);
        User buyer = userService.getByUsername("test1");

        RegisterRequest registerRequest2 = RegisterRequest.builder()
                .email(UUID.randomUUID() + "@gmail.com")
                .username("test2")
                .password("123456789")
                .build();
        userService.register(registerRequest2);
        User seller = userService.getByUsername("test2");

        NewProductRequest newProductRequest = NewProductRequest.builder()
                .image("https://i5.walmartimages.com/asr/778af350-1012-4500-95c0-7e57fdca147e.9949c90efdc7e826e386251541b47515.jpeg?odnBg=ffffff&odnHeight=1000&odnWidth=1000")
                .price(BigDecimal.valueOf(89.99))
                .name("Beats Studio Buds")
                .quantity(30)
                .description("High-quality Bluetooth earbuds with active noise cancellation, immersive sound, and a 24-hour battery life in a compact charging case.")
                .category(categoryService.getByName("ELECTRONICS"))
                .build();
        productService.addNewProduct(newProductRequest, seller);
        Product product = productService.getAllProducts().get(0);

        orderItemService.addNewOrderItem(buyer,product);
        List<OrderItem> orderItems = orderItemService.getOrderItemsByUser(buyer);
        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);

        doNothing().when(emailService).saveNotificationPreference(any(), anyBoolean(), anyString());


        //When
        orderService.makeAnOrder(orderItems,buyer,totalPrice);

        //Then

        //Check if user has an order
        List<Order> userOrders = orderService.getOrdersByUser(buyer);
        assertEquals(1, userOrders.size());

        //Check if the created order is with correct data
        Order createdOrder = userOrders.get(0);
        assertEquals(0, createdOrder.getTotalPrice().compareTo(totalPrice));
        assertEquals(createdOrder.getBuyer().getUsername(), buyer.getUsername());

        //Check if the order item in the order is with correct data
        List<OrderItem> orderItemsInOrder = orderItemService.getOrderItemsByOrderId(createdOrder);
        assertEquals(1, orderItemsInOrder.size());
        assertEquals("Beats Studio Buds", orderItemsInOrder.get(0).getProduct().getName());

        //Check if the sold product has updated quantity
        Product updatedProduct = productService.getById(product.getId());
        assertEquals(29, updatedProduct.getQuantity());

        // Verify email notification was sent
        verify(emailService, times(1)).sendNotification(
                eq(seller.getId()),
                eq("Product Sold Notification"),
                contains("Congratulations! You have successfully sold")
        );

    }
}
