package app.scheduler;

import app.products.model.Product;
import app.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class HideProductAfterOutOfStockScheduler {

    private final ProductService productService;

    @Autowired
    public HideProductAfterOutOfStockScheduler(ProductService productService) {
        this.productService = productService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void hideProductAfterOutOfStock() {
        LocalDateTime fourHoursAgo = LocalDateTime.now().minusMinutes(1);
        List<Product> productsToHide = productService.getAllProductsOutOfStockForTheTimeLimit(fourHoursAgo);

        if(productsToHide.isEmpty()) {
            log.info("No products to hide");
            return;
        }

        for(Product product : productsToHide) {
            productService.hideProduct(product);
        }
    }
}
