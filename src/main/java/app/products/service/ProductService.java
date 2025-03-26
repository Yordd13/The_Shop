package app.products.service;


import app.category.model.Category;
import app.exception.DomainException;
import app.products.model.Product;
import app.products.repository.ProductRepository;
import app.user.model.User;
import app.web.dto.NewProductRequest;
import app.web.dto.OrderProductsQuantityDecreaseEvent;
import app.web.dto.UpdateQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getById(UUID productId) {
        return productRepository.findById(productId).orElseThrow(() ->
                new DomainException("Product with this id not found!"));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addNewProduct(NewProductRequest newProductRequest, User user) {

        if (user.isBannedFromSelling()) {
            throw new DomainException("You are banned from selling!");
        }

        Product product = Product
                .builder()
                .name(newProductRequest.getName())
                .description(newProductRequest.getDescription())
                .listedOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .price(newProductRequest.getPrice())
                .image(newProductRequest.getImage())
                .category(newProductRequest.getCategory())
                .quantity(newProductRequest.getQuantity())
                .seller(user)
                .isVisible(true)
                .build();

        productRepository.save(product);
    }

    @EventListener
    @Async
    public void decreaseProductsQuantityAfterNewOrder(OrderProductsQuantityDecreaseEvent event) {

        Map<UUID, Integer> productQuantityMap = event.getProductQuantityMap();

        productQuantityMap.forEach(this::decreaseProductQuantity);
    }

    private void decreaseProductQuantity(UUID productId, int quantity) {
        Product product = getById(productId);
        if(product.getQuantity() < quantity) {
            throw new DomainException("Product with id [%s] has less quantity that the requested!".formatted(productId));
        }
        product.setQuantity(product.getQuantity() - quantity);

        productRepository.save(product);
    }

    public List<Product> searchProductsByCategory(Category category, String search) {
        return productRepository.findByCategoryAndNameContainingIgnoreCase(category,search);
    }

    public void deleteProductsByUser(User user) {
        List<Product> products = productRepository.getProductsBySeller(user);
        products.forEach(product -> {
            product.setVisible(false);
            productRepository.save(product);
        });
    }

    public List<Product> getAllProductsOutOfStockForTheTimeLimit(LocalDateTime fourHoursAgo) {
        return productRepository.findOutOfStockBefore(fourHoursAgo);
    }

    public void hideProduct(Product product) {
        product.setVisible(false);
        productRepository.save(product);
    }

    public void updateQuantity(Product product, UpdateQuantityRequest updateQuantityRequest) {
        product.setQuantity
                (product.getQuantity() + updateQuantityRequest.getQuantity());
        product.setVisible(true);
        productRepository.save(product);
    }
}

