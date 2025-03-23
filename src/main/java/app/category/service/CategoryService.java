package app.category.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import app.products.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void addCategory(String categoryName, String categoryPictureUrl, String description) {
        Category category = new Category(categoryName, categoryPictureUrl, description);
        categoryRepository.save(category);
    }

    public Category getByName(String name) {
        return categoryRepository.getByCategoryName(name);
    }

    public Map<UUID, Integer> getActiveProductsCount() {
        List<Category> categories = getAllCategories();
        Map<UUID, Integer> activeProductCounts = new HashMap<>();

        for (Category category : categories) {
            int count = (int) category.getProducts().stream()
                    .filter(Product::isVisible)
                    .count();
            activeProductCounts.put(category.getId(), count);
        }

        return activeProductCounts;
    }
}
