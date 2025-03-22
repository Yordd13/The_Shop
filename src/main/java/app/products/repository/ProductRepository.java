package app.products.repository;

import app.category.model.Category;
import app.products.model.Product;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryAndNameContainingIgnoreCase(Category category, String search);

    List<Product> getProductsBySeller(User user);
}
