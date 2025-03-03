package app.orderItem.service;

import app.orderItem.model.OrderItem;
import app.orderItem.repository.OrderItemRepository;
import app.products.model.Product;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public void addNewOrderItem(User user, Product product) {

        orderItemRepository.findByProductIdAndUserId(product.getId(), user.getId())
                .ifPresentOrElse(
                        orderItem -> {
                            if (orderItem.getQuantity() + 1 > product.getQuantity()) {
                                throw new IllegalStateException("Cannot add more items: product stock limit reached.");
                            }
                            orderItem.setQuantity(orderItem.getQuantity() + 1);
                            orderItemRepository.save(orderItem);
                        },
                        () -> {
                            if (product.getQuantity() <= 0) {
                                throw new IllegalStateException("Cannot create order item: product is out of stock.");
                            }
                            OrderItem newOrderItem = OrderItem.builder()
                                    .user(user)
                                    .product(product)
                                    .quantity(1)
                                    .build();
                            orderItemRepository.save(newOrderItem);
                        }
                );

    }
}
