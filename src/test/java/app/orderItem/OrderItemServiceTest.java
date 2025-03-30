package app.orderItem;

import app.email.service.EmailService;
import app.exception.DomainException;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.orderItem.repository.OrderItemRepository;
import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.user.model.User;
import app.web.dto.UserOrderItemsToOrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {

    @Mock
    private  OrderItemRepository orderItemRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void givenNoExistingOrderItemAndStockIsPositive_whenAddNewOrderItem_thenCreateNewOrderItem() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .quantity(5)
                .build();

        when(orderItemRepository.findByProductIdAndUserIdAndOrderIsNull(product.getId(), user.getId()))
                .thenReturn(Optional.empty());

        // When
        orderItemService.addNewOrderItem(user, product);

        // Then
        verify(orderItemRepository,times(1)).save(any(OrderItem.class));

    }

    @Test
    void givenNoExistingOrderItemAndOutOfStockProduct_whenAddNewOrderItem_thenThrowException() {

        // Given
        User user = User.builder().id(UUID.randomUUID()).build();
        Product product = Product.builder().id(UUID.randomUUID()).quantity(0).build();

        when(orderItemRepository.findByProductIdAndUserIdAndOrderIsNull(product.getId(), user.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalStateException.class, () -> orderItemService.addNewOrderItem(user, product));
        verify(orderItemRepository, never()).save(any(OrderItem.class));

    }

    @Test
    void givenExistingOrderItemAndStockAvailable_whenAddNewOrderItem_thenIncreaseQuantity() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .quantity(5)
                .build();
        OrderItem existingOrderItem = OrderItem.builder()
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        when(orderItemRepository.findByProductIdAndUserIdAndOrderIsNull(product.getId(), user.getId()))
                .thenReturn(Optional.of(existingOrderItem));

        // When
        orderItemService.addNewOrderItem(user, product);

        // Then
        assertEquals(3, existingOrderItem.getQuantity());
        verify(orderItemRepository).save(existingOrderItem);

    }

    @Test
    void givenExistingOrderItemAndStockLimitReached_whenAddNewOrderItem_thenDoNothing() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .quantity(2)
                .build();
        OrderItem existingOrderItem = OrderItem.builder()
                .user(user)
                .product(product)
                .quantity(2)
                .build();

        when(orderItemRepository.findByProductIdAndUserIdAndOrderIsNull(product.getId(), user.getId()))
                .thenReturn(Optional.of(existingOrderItem));

        // When
        orderItemService.addNewOrderItem(user, product);

        // Then
        assertEquals(2, existingOrderItem.getQuantity());
        verify(orderItemRepository, never()).save(existingOrderItem);

    }

    @Test
    void givenOrderItemAndStockLimitNotReached_whenIncreaseQuantity_thenIncreaseQuantity() {

        // Given
        UUID orderItemId = UUID.randomUUID();
        Product product = Product.builder()
                .quantity(5)
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .product(product)
                .quantity(3)
                .build();

        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.of(orderItem));

        // When
        orderItemService.increaseQuantity(orderItemId);

        // Then
        assertEquals(4, orderItem.getQuantity());
        verify(orderItemRepository).save(orderItem);

    }

    @Test
    void givenOrderItemAndStockLimitIsReached_whenIncreaseQuantity_thenDoNothing() {

        //Given
        UUID orderItemId = UUID.randomUUID();
        Product product = Product.builder()
                .quantity(3)
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .product(product)
                .quantity(3)
                .build();
        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.of(orderItem));

        //When
        orderItemService.increaseQuantity(orderItemId);

        //Then
        assertEquals(3, orderItem.getQuantity());
        verify(orderItemRepository, never()).save(orderItem);

    }

    @Test
    void givenNonExistingOrderItem_whenIncreaseQuantity_thenThrowDomainException() {

        // Given
        UUID orderItemId = UUID.randomUUID();
        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.empty());

        //When and Then
        assertThrows(DomainException.class, () -> orderItemService.increaseQuantity(orderItemId));
        verify(orderItemRepository, never()).save(any());

    }

    @Test
    void givenOrderItemWithQuantityGreaterThanOne_whenDecreaseQuantity_thenDecreaseQuantity() {

        // Given
        UUID orderItemId = UUID.randomUUID();
        Product product = Product.builder()
                .quantity(5)
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .product(product)
                .quantity(3)
                .build();

        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.of(orderItem));

        // When
        orderItemService.decreaseQuantity(orderItemId);

        // Then
        assertEquals(2, orderItem.getQuantity());
        verify(orderItemRepository).save(orderItem);
        verify(orderItemRepository, never()).delete(any());

    }

    @Test
    void givenOrderItemWithQuantityOne_whenDecreaseQuantity_thenDeleteOrderItem() {

        // Given
        UUID orderItemId = UUID.randomUUID();
        Product product = Product.builder()
                .quantity(5)
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .product(product)
                .quantity(1)
                .build();

        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.of(orderItem));

        // When
        orderItemService.decreaseQuantity(orderItemId);

        // Then
        verify(orderItemRepository).delete(orderItem);
        verify(orderItemRepository, never()).save(any());

    }

    @Test
    void givenNonExistingOrderItem_whenDecreaseQuantity_thenThrowDomainException() {

        // Given
        UUID orderItemId = UUID.randomUUID();
        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> orderItemService.decreaseQuantity(orderItemId));
        verify(orderItemRepository, never()).save(any());
        verify(orderItemRepository, never()).delete(any());

    }

    @Test
    void givenMultipleOrderItems_whenGetTotalPrice_thenCalculateCorrectly() {

        // Given
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(10))
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(20))
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

        // When
        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);

        // Then
        assertEquals(BigDecimal.valueOf(80), totalPrice);

    }

    @Test
    void givenEmptyOrderItemsList_whenGetTotalPrice_thenReturnZero() {

        // Given
        List<OrderItem> orderItems = List.of();

        // When
        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);

        // Then
        assertEquals(BigDecimal.ZERO, totalPrice);

    }

    @Test
    void givenUserOrderItemsEvent_whenAddOrderIdToOrderItems_thenAssignOrderAndSendEmails() {

        // Given
        User seller = User.builder()
                .id(UUID.randomUUID())
                .build();
        User buyer = User.builder()
                .id(UUID.randomUUID())
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .seller(seller)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .user(buyer)
                .build();

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .buyer(buyer)
                .build();

        UserOrderItemsToOrderEvent event = new UserOrderItemsToOrderEvent(buyer, order);
        when(orderItemService.getOrderItemsByUser(buyer))
                .thenReturn(List.of(orderItem));

        // When
        orderItemService.addOrderIdToOrderItems(event);

        // Then
        assertEquals(order, orderItem.getOrder());

        verify(emailService, times(1)).sendNotification(
                eq(seller.getId()),
                eq("Product Sold Notification"),
                contains("Congratulations! You have successfully sold 2 units of your product \"Laptop\"")
        );

        verify(orderItemRepository, times(1)).save(orderItem);

    }

    @Test
    void givenOrderItem_whenDeleteOrderItem_thenDeleteOrderItem() {

        //Given
        UUID orderItemId = UUID.randomUUID();
        Product product = Product.builder()
                .quantity(5)
                .build();
        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .product(product)
                .quantity(1)
                .build();

        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.of(orderItem));

        //When
        orderItemService.deleteOrderItem(orderItemId);

        //Then
        verify(orderItemRepository).delete(orderItem);

    }

    @Test
    void givenOrderItemThatDoesNotExist_whenDeleteOrderItem_thenThrowDomainException() {

        //Given
        UUID orderItemId = UUID.randomUUID();
        when(orderItemRepository.findById(orderItemId))
                .thenReturn(Optional.empty());

        //When and Then
        assertThrows(DomainException.class, () -> orderItemService.deleteOrderItem(orderItemId));

    }

    @Test
    void givenUser_whenDeleteOrderItemsByUserThatAreNull_thenDeleteThem() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        OrderItem orderItem1 = OrderItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .build();

        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        when(orderItemRepository.deleteOrderItemsByUser(user))
                .thenReturn(orderItems);

        // When
        orderItemService.deleteOrderItemsByUserThatAreNull(user);

        // Then
        verify(orderItemRepository, times(1)).deleteOrderItemsByUser(user);
        verify(orderItemRepository, times(1)).deleteAll(orderItems);

    }

    @Test
    void givenListOfOrderItems_whenGetCountOfTheirOverallQuantity_thenReturnIt() {

        //Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        OrderItem orderItem1 = OrderItem.builder()
                .id(UUID.randomUUID())
                .user(user)
                .quantity(2)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .id(UUID.randomUUID())
                .quantity(1)
                .user(user)
                .build();

        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        // When and Then
        int result = orderItemService.getCountOfOrderItems(orderItems);
        assertEquals(3, result);

    }

    @Test
    void givenOrderItems_whenGetTotalProfit_thenReturnCorrectTotalProfit() {

        // Given
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(50))
                .build();
        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(100))
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .product(product1)
                .quantity(2)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .product(product2)
                .quantity(1)
                .build();

        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        // When
        BigDecimal totalProfit = orderItemService.getTotalProfit(orderItems);

        // Then
        assertEquals(BigDecimal.valueOf(200), totalProfit);

    }

    @Test
    void givenEmptyOrderItems_whenGetTotalProfit_thenReturnZero() {

        // Given
        List<OrderItem> emptyOrderItems = List.of();

        // When
        BigDecimal totalProfit = orderItemService.getTotalProfit(emptyOrderItems);

        // Then
        assertEquals(BigDecimal.ZERO, totalProfit);

    }

    @Test
    void givenUserAndTime_whenGetSales_thenReturnCorrectCount() {

        // Given
        User user = new User();
        LocalDateTime timestamp = LocalDateTime.now();

        OrderItem orderItem1 = OrderItem.builder()
                .quantity(2)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .quantity(3)
                .build();

        List<OrderItem> mockOrderItems = List.of(orderItem1, orderItem2);
        when(orderItemRepository.getOrderItemsByUserAndTimestamp(user, timestamp))
                .thenReturn(mockOrderItems);

        // When and Then
        int salesCount = orderItemService.getSales(user, timestamp);
        assertEquals(5, salesCount);

    }

    @Test
    void givenUserAndTime_whenGetProfit_thenReturnCorrectTotalProfit() {

        // Given
        User user = new User();
        LocalDateTime timestamp = LocalDateTime.now();

        Product product1 = Product.builder()
                .price(BigDecimal.valueOf(50))
                .build();
        Product product2 = Product.builder()
                .price(BigDecimal.valueOf(100))
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .product(product1)
                .quantity(2)
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .product(product2)
                .quantity(1)
                .build();

        List<OrderItem> mockOrderItems = List.of(orderItem1, orderItem2);

        when(orderItemRepository.getOrderItemsByUserAndTimestamp(user, timestamp))
                .thenReturn(mockOrderItems);

        // When
        BigDecimal totalProfit = orderItemService.getProfit(user, timestamp);

        // Then
        assertEquals(BigDecimal.valueOf(200), totalProfit);

    }

    @Test
    void givenProductWithOrderNotNull_whenContainsProductWithOrderNotNull_thenReturnTrue() {

        // Given
        Product product = new Product();
        OrderItem orderItem = OrderItem.builder().build();

        when(orderItemRepository.getOrderItemsByProductWhereOrderIsNotNull(product))
                .thenReturn(List.of(orderItem));

        // When and Then
        boolean result = orderItemService.containsProductWithOrderNotNull(product);
        assertTrue(result);
        verify(orderItemRepository).getOrderItemsByProductWhereOrderIsNotNull(product);
        verifyNoMoreInteractions(orderItemRepository);

    }

    @Test
    void givenProductWithNoOrdersButHasOrderItems_whenContainsProductWithOrderNotNull_thenDeleteOrderItemsAndReturnFalse() {

        // Given
        Product product = new Product();
        OrderItem orderItem1 = OrderItem.builder()
                .id(UUID.randomUUID())
                .build();
        OrderItem orderItem2 = OrderItem.builder()
                .id(UUID.randomUUID())
                .build();

        when(orderItemRepository.getOrderItemsByProductWhereOrderIsNotNull(product))
                .thenReturn(List.of());
        when(orderItemRepository.getOrderItemsByProductWhereOrderIsNull(product))
                .thenReturn(List.of(orderItem1, orderItem2));
        when(orderItemRepository.findById(orderItem2.getId()))
                .thenReturn(Optional.of(orderItem2));
        when(orderItemRepository.findById(orderItem1.getId()))
                .thenReturn(Optional.of(orderItem1));

        // When and Then
        boolean result = orderItemService.containsProductWithOrderNotNull(product);
        assertFalse(result);
        verify(orderItemRepository).getOrderItemsByProductWhereOrderIsNotNull(product);
        verify(orderItemRepository).getOrderItemsByProductWhereOrderIsNull(product);
        verify(orderItemRepository).delete(orderItem2);
        verify(orderItemRepository).delete(orderItem1);

    }

    @Test
    void givenProductWithNoOrdersAndNoOrderItems_whenContainsProductWithOrderNotNull_thenReturnFalse() {

        // Given
        Product product = new Product();

        when(orderItemRepository.getOrderItemsByProductWhereOrderIsNotNull(product))
                .thenReturn(List.of());
        when(orderItemRepository.getOrderItemsByProductWhereOrderIsNull(product))
                .thenReturn(List.of());

        // When and Then
        boolean result = orderItemService.containsProductWithOrderNotNull(product);
        assertFalse(result);
        verify(orderItemRepository).getOrderItemsByProductWhereOrderIsNotNull(product);
        verify(orderItemRepository).getOrderItemsByProductWhereOrderIsNull(product);
        verifyNoMoreInteractions(orderItemRepository);

    }
}
