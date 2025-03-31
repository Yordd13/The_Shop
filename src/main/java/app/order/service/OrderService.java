package app.order.service;

import app.exception.DomainException;
import app.order.model.Order;
import app.order.repository.OrderRepository;
import app.orderItem.model.OrderItem;
import app.user.model.User;
import app.web.dto.OrderProductsQuantityDecreaseEvent;
import app.web.dto.UserOrderItemsToOrderEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderService(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void makeAnOrder(List<OrderItem> orderItems, User user, BigDecimal totalPrice) {
        if(orderItems.isEmpty()) return;

        Order order = Order
                .builder()
                .buyer(user)
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .build();

        orderRepository.save(order);

        eventPublisher.publishEvent(new UserOrderItemsToOrderEvent(user,order));

        Map<UUID, Integer> productQuantityMap = orderItems.stream()
                .collect(Collectors.toMap(
                        orderItem -> orderItem.getProduct().getId(), // Key: productId
                        OrderItem::getQuantity                     // Value: quantity of the ordered product
                ));

        eventPublisher.publishEvent(new OrderProductsQuantityDecreaseEvent(productQuantityMap));
    }

    public Order getById(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(()
                -> new DomainException("Cannot find order with id: " + orderId));
    }

    public List<Order> getOrdersByUser(User user){
        return orderRepository.findAllByBuyer(user);
    }
}
