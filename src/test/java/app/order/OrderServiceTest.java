package app.order;

import app.order.repository.OrderRepository;
import app.order.service.OrderService;
import app.orderItem.model.OrderItem;
import app.products.model.Product;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void givenValidOrderItems_whenMakeAnOrder_thenSaveOrderAndPublishEvents() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .product(product1)
                .quantity(2)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .product(product2)
                .quantity(3)
                .build();

        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
        BigDecimal totalPrice = BigDecimal.valueOf(100);

        // When
        orderService.makeAnOrder(orderItems, user, totalPrice);

        // Then
        verify(orderRepository).save(argThat(order ->
                order.getBuyer().equals(user) &&
                        order.getTotalPrice().equals(totalPrice) &&
                        order.getOrderDate() != null
        ));

    }

    @Test
    void givenEmptyOrderItems_whenMakeAnOrder_thenDoNothing() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        List<OrderItem> orderItems = List.of();
        BigDecimal totalPrice = BigDecimal.valueOf(100);

        // When
        orderService.makeAnOrder(orderItems, user, totalPrice);

        // Then
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(eventPublisher);

    }
}
