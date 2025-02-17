package app.category.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategoryInit implements CommandLineRunner {

    private final CategoryService categoryService;

    @Autowired
    public CategoryInit(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(!categoryService.getAllCategories().isEmpty()) {
            return;
        }

        categoryService.addCategory(
                "ELECTRONICS",
                "https://cdn.pixabay.com/photo/2019/12/27/01/48/samsung-4721549_1280.jpg",
                "Electronics and gadgets including phones, laptops, and accessories."
        );

        categoryService.addCategory(
                "FASHION",
                "https://cdn.pixabay.com/photo/2017/04/06/11/24/fashion-2208045_1280.jpg",
                "Trendy clothing, shoes, and accessories for all styles."
        );

        categoryService.addCategory(
                "BOOKS",
                "https://cdn.pixabay.com/photo/2015/11/19/21/10/glasses-1052010_1280.jpg",
                "A wide selection of books across various genres."
        );

        categoryService.addCategory(
                "TOYS",
                "https://cdn.pixabay.com/photo/2024/06/23/11/16/toy-connector-8847968_1280.jpg",
                "Toys for children of all ages, from building blocks to action figures."
        );

        categoryService.addCategory(
                "SPORTS",
                "https://cdn.pixabay.com/photo/2022/06/18/18/05/skateboard-7270418_1280.jpg",
                "Sports equipment, clothing, and accessories for enthusiasts."
        );

        categoryService.addCategory(
                "AUTOMOTIVE",
                "https://cdn.pixabay.com/photo/2019/10/29/12/21/motor-4586782_1280.jpg",
                "Automotive parts, tools, and accessories for car maintenance."
        );

    }
}
