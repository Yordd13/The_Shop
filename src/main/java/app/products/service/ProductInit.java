package app.products.service;

import app.category.service.CategoryService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewProductRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(3)
@Profile("!test")
public class ProductInit implements CommandLineRunner {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public ProductInit(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(!productService.getAllProducts().isEmpty()) {
            return;
        }

        User seller = userService.getByUsername("admin123");
        userService.addSellerRole(seller);

        NewProductRequest newProductRequest1 = NewProductRequest
                .builder()
                .image("https://i5.walmartimages.com/asr/778af350-1012-4500-95c0-7e57fdca147e.9949c90efdc7e826e386251541b47515.jpeg?odnBg=ffffff&odnHeight=1000&odnWidth=1000")
                .price(BigDecimal.valueOf(89.99))
                .name("Beats Studio Buds")
                .quantity(30)
                .description("High-quality Bluetooth earbuds with active noise cancellation, immersive sound, and a 24-hour battery life in a compact charging case.")
                .category(categoryService.getByName("ELECTRONICS"))
                .build();

        productService.addNewProduct(newProductRequest1, seller);

        NewProductRequest newProductRequest2 = NewProductRequest
                .builder()
                .image("https://www.electrical-deals.co.uk/media/catalog/product/cache/1/image/1800x/040ec09b1e35df139433887a97daa66f/5/5/55U6863DB-left.jpg")
                .price(BigDecimal.valueOf(599.99))
                .name("Smart LED TV")
                .quantity(20)
                .description("Stunning 55-inch 4K UHD Smart TV with HDR support, voice control, and built-in streaming apps for endless entertainment.")
                .category(categoryService.getByName("ELECTRONICS"))
                .build();

        productService.addNewProduct(newProductRequest2, seller);

        NewProductRequest newProductRequest3 = NewProductRequest
                .builder()
                .image("https://images-na.ssl-images-amazon.com/images/I/71o%2B3a3EtUL._AC_SL1500_.jpg")
                .price(BigDecimal.valueOf(79.99))
                .name("Dash Cam")
                .quantity(50)
                .description("HD dash cam with loop recording, motion detection, and infrared night vision to ensure safety on the road day and night.")
                .category(categoryService.getByName("AUTOMOTIVE"))
                .build();

        productService.addNewProduct(newProductRequest3, seller);

        NewProductRequest newProductRequest4 = NewProductRequest
                .builder()
                .image("https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1492480066i/31556125.jpg")
                .price(BigDecimal.valueOf(19.99))
                .name("Self-Improvement Book")
                .quantity(100)
                .description("A must-read guide to boosting focus, eliminating distractions, and achieving goals with smart time-management techniques.")
                .category(categoryService.getByName("BOOKS"))
                .build();

        productService.addNewProduct(newProductRequest4, seller);

        NewProductRequest newProductRequest5 = NewProductRequest
                .builder()
                .image("https://sayarastclair.com/wp-content/uploads/2018/04/FINAL-as-of-3-2-2018-MysticRealms-3D-ebook-Cover-2-683x1024.jpg")
                .price(BigDecimal.valueOf(14.99))
                .name("Fantasy Novel")
                .quantity(80)
                .description("An epic tale of magic, destiny, and adventure, following a young hero's journey through a world of mystical creatures and hidden powers.")
                .category(categoryService.getByName("BOOKS"))
                .build();

        productService.addNewProduct(newProductRequest5, seller);

        NewProductRequest newProductRequest6 = NewProductRequest
                .builder()
                .image("https://cdn.shopify.com/s/files/1/2608/0936/products/Leather_Mens_Small_Wallet_Slim_Trifold_Vintage_Wallet_for_Mens_7_1024x1024.jpg?v=1571611908")
                .price(BigDecimal.valueOf(29.99))
                .name("Classic Leather Wallet")
                .quantity(60)
                .description("Stylish and durable leather wallet with RFID protection, multiple card slots, and a sleek design for everyday use.")
                .category(categoryService.getByName("FASHION"))
                .build();

        productService.addNewProduct(newProductRequest6, seller);

        NewProductRequest newProductRequest7 = NewProductRequest
                .builder()
                .image("https://m.media-amazon.com/images/I/71C-nuAyCPL._AC_SL1095_.jpg")
                .price(BigDecimal.valueOf(49.99))
                .name("Building Block Set")
                .quantity(1)
                .description("A creative and fun building block set with 1,500 colorful pieces, perfect for kids and adults to build anything they imagine.")
                .category(categoryService.getByName("TOYS"))
                .build();

        productService.addNewProduct(newProductRequest7, seller);
    }
}
