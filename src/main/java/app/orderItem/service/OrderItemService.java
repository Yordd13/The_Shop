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

        if (product.getQuantity() <= 0) {
            throw new IllegalStateException("Product is out of stock.");
        }

        orderItemRepository.findByProductId(product.getId())
                .ifPresentOrElse(
                        orderItem -> {
                            if (orderItem.getQuantity() + 1 > product.getQuantity()) {
                                throw new IllegalStateException("Cannot add more items: product stock limit reached.");
                            }
                            orderItem.setQuantity(orderItem.getQuantity() + 1);
                            orderItemRepository.save(orderItem);
                        },
                        () -> {
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
