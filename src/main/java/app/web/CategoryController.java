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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
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

        int cartQuantity = userService.getOrderQuantity(user);

        Category category = categoryService.getByName(name.toUpperCase());

        List<Product> productsByCategory = category.getProducts()
                .stream()
                .filter(Product::isVisible)
                .collect(Collectors.toList());

        ModelAndView mav = new ModelAndView();
        mav.setViewName("category");
        mav.addObject("user", user);
        mav.addObject("category", category);
        mav.addObject("products", productsByCategory);
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }
}
