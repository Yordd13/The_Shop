package app.products.service;


import app.category.model.Category;
import app.exception.DomainException;
import app.products.model.Product;
import app.products.repository.ProductRepository;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    public void addProduct(String name, BigDecimal price, int quantity, String description, String image, User seller, Category category) {
        Product product = Product
                .builder()
                .name(name)
                .price(price)
                .quantity(quantity)
                .description(description)
                .image(image)
                .seller(seller)
                .listedOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .categories(List.of(category))
                .build();
        productRepository.save(product);
    }
}

