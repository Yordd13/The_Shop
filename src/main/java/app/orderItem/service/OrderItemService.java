package app.orderItem.service;

import app.exception.DomainException;
import app.orderItem.model.OrderItem;
import app.orderItem.repository.OrderItemRepository;
import app.products.model.Product;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
}
