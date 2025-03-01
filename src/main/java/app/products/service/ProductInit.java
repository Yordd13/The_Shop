package app.products.service;


import app.category.model.Category;
import app.category.service.CategoryService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProductInit implements CommandLineRunner {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;

    @Autowired
    public ProductInit(ProductService productService, UserService userService, CategoryService categoryService) {
        this.productService = productService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(!productService.getAllProducts().isEmpty()){
            return;
        }

        User user = userService.getById(UUID.fromString("a2f568dc-04e6-43e1-8c71-08185368f471"));

        Category category = categoryService.getByName("TOYS");

        productService.addProduct("TEST", BigDecimal.valueOf(10.00),10,
                "TOY TEST AAAAAAAAAAAAA AAAAAAAAAAAAAAAAAA AAAAAAAAAAAAAAAAA AAAAAAAAAAAA AAAAAAAAAAAAAAAAAAA" +
                        "AAAAAAAAAAAAA AAAAAAAAAAAAAA AAAAAAAA AAAAAAAAAAA AAAAAAAAAAAAAAAAAAAAAAA" +
                        "AAAAAAAA AAAAAAAAAAAA AAAAAAAAAAAAAAAAAAAA mersi TEST","https://www.manhattantoy.com/cdn/shop/products/y09ydoqdjmgjx8meu9b9.jpg?v=1590504674&width=1080",
                user,category);
    }
}