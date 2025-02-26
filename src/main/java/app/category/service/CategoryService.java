package app.category.service;

import app.category.model.Category;
import app.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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
}
