package app.orderItem.repository;

import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT o FROM OrderItem o WHERE o.product.id = :productId AND o.user.id = :userId AND o.order IS NULL ")
    Optional<OrderItem> findByProductIdAndUserIdAndVisible(
            @Param("productId") UUID productId,
            @Param("userId") UUID userId
    );

    List<OrderItem> findByUser(User user);

    List<OrderItem> findByOrder(Order order);
}
