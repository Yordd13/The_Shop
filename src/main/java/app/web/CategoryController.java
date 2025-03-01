package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.products.model.Product;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private CategoryService categoryService;
    private UserService userService;

    @Autowired
    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/{name}")
    public ModelAndView getCategory(@PathVariable String name, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        Category category = categoryService.getByName(name.toUpperCase());

        List<Product> productsByCategory = category.getProducts();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("category");
        modelAndView.addObject("user", user);
        modelAndView.addObject("category", category);
        modelAndView.addObject("products", productsByCategory);

        return modelAndView;
    }
}
