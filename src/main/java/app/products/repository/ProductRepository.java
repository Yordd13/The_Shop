package app.products.repository;

import app.category.model.Category;
import app.products.model.Product;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryAndNameContainingIgnoreCase(Category category, String search);

    List<Product> getProductsBySeller(User user);

    @Query("SELECT p FROM Product p WHERE p.quantity = 0 AND p.isVisible = true AND p.updatedOn <= :timeLimit")
    List<Product> findOutOfStockBefore(LocalDateTime timeLimit);
}
