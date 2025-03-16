package app.orderItem.service;

import app.email.service.EmailService;
import app.exception.DomainException;
import app.orderItem.model.OrderItem;
import app.orderItem.repository.OrderItemRepository;
import app.products.model.Product;
import app.user.model.User;
import app.web.dto.UserOrderItemsToOrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, EmailService emailService) {
        this.orderItemRepository = orderItemRepository;
        this.emailService = emailService;
    }

    public void addNewOrderItem(User user, Product product) {

        Optional<OrderItem> orderItemOptional = orderItemRepository.findByProductIdAndUserIdAndVisible(product.getId(), user.getId());

        if(orderItemOptional.isEmpty()) {
            if (product.getQuantity() <= 0) {
                throw new IllegalStateException("Cannot create order item: product is out of stock.");
            }
            OrderItem newOrderItem = OrderItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(1)
                    .order(null)
                    .build();
            orderItemRepository.save(newOrderItem);
            return;
        }

        OrderItem orderItem = orderItemOptional.get();

        if (orderItem.getQuantity() + 1 > product.getQuantity()) {
            return;
        }

        orderItem.setQuantity(orderItem.getQuantity() + 1);
        orderItemRepository.save(orderItem);

    }

    public OrderItem getOrderItem(UUID id) {
        return orderItemRepository.findById(id).orElseThrow(()
                -> new DomainException("Cannot find order item with id: " + id));
    }

    public void increaseQuantity(UUID orderItemId) {
        OrderItem orderItem = getOrderItem(orderItemId);
        if(orderItem.getQuantity() < orderItem.getProduct().getQuantity()) {
            orderItem.setQuantity(orderItem.getQuantity() + 1);
        }
        orderItemRepository.save(orderItem);
    }

    public void decreaseQuantity(UUID orderItemId) {
        OrderItem orderItem = getOrderItem(orderItemId);
        if(orderItem.getQuantity() > 1){
            orderItem.setQuantity(orderItem.getQuantity() - 1);
            orderItemRepository.save(orderItem);
            return;
        }

        orderItemRepository.delete(orderItem);
    }

    public BigDecimal getTotalPrice(List<OrderItem> orderItems) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            totalPrice = totalPrice
                    .add(orderItem.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        return totalPrice;
    }

    public List<OrderItem> getOrderItemsByUser(User user) {
        return orderItemRepository.findByUser(user);
    }

    @EventListener
    @Async
    public void addOrderIdToOrderItems(UserOrderItemsToOrderEvent event){
        List<OrderItem> orderItems = getOrderItemsByUser(event.getUser());

        orderItems.forEach(orderItem -> {

            String body = "You successfully sold " + orderItem.getQuantity() + " items from your product " + orderItem.getProduct().getName();

            emailService.sendNotification(orderItem.getProduct().getSeller().getId(),"Sold product",body);


            orderItem.setOrder(event.getOrder());
            orderItemRepository.save(orderItem);
        });
    }

}
