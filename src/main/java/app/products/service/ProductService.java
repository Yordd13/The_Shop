package app.products.service;


import app.category.model.Category;
import app.exception.DomainException;
import app.products.model.Product;
import app.products.repository.ProductRepository;
import app.user.model.User;
import app.web.dto.NewProductRequest;
import jakarta.validation.Valid;
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

    public void addNewProduct(NewProductRequest newProductRequest, User user) {
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
                .build();

        productRepository.save(product);
    }
}

