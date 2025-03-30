package app.orderItem.repository;

import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.products.model.Product;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT o FROM OrderItem o WHERE o.product.id = :productId AND o.user.id = :userId AND o.order IS NULL ")
    Optional<OrderItem> findByProductIdAndUserIdAndOrderIsNull(
            @Param("productId") UUID productId,
            @Param("userId") UUID userId
    );
    @Query("SELECT o FROM OrderItem o WHERE o.user = :user AND o.order IS NULL")
    List<OrderItem> findByUserWhereOrderIsNull(User user);

    List<OrderItem> findByOrder(Order order);

    @Query("SELECT o FROM OrderItem o WHERE o.product.seller = :user AND o.order IS NULL")
    List<OrderItem> deleteOrderItemsByUser(User user);

    @Query("SELECT o FROM OrderItem o JOIN o.order ord WHERE o.product.seller = :user AND ord.orderDate >= :timestamp")
    List<OrderItem> getOrderItemsByUserAndTimestamp(User user, LocalDateTime timestamp);

    @Query("SELECT o FROM OrderItem o WHERE o.product = :product AND o.order IS NOT NULL")
    List<OrderItem> getOrderItemsByProductWhereOrderIsNotNull(Product product);

    @Query("SELECT o FROM OrderItem o WHERE o.product = :product AND o.order IS NULL")
    List<OrderItem> getOrderItemsByProductWhereOrderIsNull(Product product);
}
