package app.orderItem.service;

import app.email.service.EmailService;
import app.exception.DomainException;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.orderItem.repository.OrderItemRepository;
import app.products.model.Product;
import app.user.model.User;
import app.web.dto.UserOrderItemsToOrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        Optional<OrderItem> orderItemOptional = orderItemRepository.findByProductIdAndUserIdAndOrderIsNull(product.getId(), user.getId());

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
            orderItemRepository.save(orderItem);
        }
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
        return orderItemRepository.findByUserWhereOrderIsNull(user);
    }

    //removed @Async, because when an order is made and the user is redirected to /categories the cartQuantity is not updated still
    @EventListener
    public void addOrderIdToOrderItems(UserOrderItemsToOrderEvent event){
        List<OrderItem> orderItems = getOrderItemsByUser(event.getUser());

        orderItems.forEach(orderItem -> {
            String body = String.format(
                    "Congratulations! You have successfully sold %d units of your product \"%s\". " +
                            "Thank you for using our platform, and we appreciate your continued business.",
                    orderItem.getQuantity(),
                    orderItem.getProduct().getName()
            );

            emailService.sendNotification(
                    orderItem.getProduct().getSeller().getId(),
                    "Product Sold Notification",
                    body
            );

            orderItem.setOrder(event.getOrder());
            orderItemRepository.save(orderItem);
        });

    }

    public void deleteOrderItem(UUID orderItemId) {
        OrderItem orderItem = getOrderItem(orderItemId);
        orderItemRepository.delete(orderItem);
    }

    public List<OrderItem> getOrderItemsByOrderId(Order order) {
        return  orderItemRepository.findByOrder(order);
    }

    public void deleteOrderItemsByUserThatAreNull(User user) {
        List<OrderItem> orderItems = orderItemRepository.deleteOrderItemsByUser(user);
        orderItemRepository.deleteAll(orderItems);
    }

    public int getCountOfOrderItems(List<OrderItem> orderItems) {
        int count = 0;
        for (OrderItem orderItem : orderItems) {
            count += orderItem.getQuantity();
        }
        return count;
    }

    public BigDecimal getTotalProfit(List<OrderItem> orderItems) {
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItems) {
            totalProfit = totalProfit.add(BigDecimal.valueOf(orderItem.getQuantity()).multiply(orderItem.getProduct().getPrice()));
        }
        return totalProfit;
    }

    public int getSales(User user, LocalDateTime localDateTime) {
        return getCountOfOrderItems(orderItemRepository.getOrderItemsByUserAndTimestamp(user, localDateTime));
    }

    public BigDecimal getProfit(User user, LocalDateTime localDateTime) {
        return getTotalProfit(orderItemRepository.getOrderItemsByUserAndTimestamp(user,localDateTime));
    }

    public boolean containsProductWithOrderNotNull(Product product) {
        List<OrderItem> orderItems = orderItemRepository.getOrderItemsByProductWhereOrderIsNotNull(product);
        if(orderItems.isEmpty()){
            List<OrderItem> orderItems1 = orderItemRepository.getOrderItemsByProductWhereOrderIsNull(product);
            if(!orderItems1.isEmpty()){
                for(OrderItem orderItem : orderItems1){
                    deleteOrderItem(orderItem.getId());
                }
            }
            return false;
        }
        return true;
    }
}
