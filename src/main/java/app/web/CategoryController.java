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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public CategoryController(CategoryService categoryService, UserService userService, ProductService productService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping("/{name}")
    public ModelAndView getCategory(@PathVariable String name, @RequestParam(name = "search", required = false) String search, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        int cartQuantity = userService.getOrderQuantity(user);

        Category category = categoryService.getByName(name.toUpperCase());

        List<Product> productsByCategory;

        if (search != null && !search.isEmpty()) {
            productsByCategory = productService.searchProductsByCategory(category,search)
                    .stream().filter(Product::isVisible).collect(Collectors.toList());
        } else {
            productsByCategory = category.getProducts()
                    .stream()
                    .filter(Product::isVisible)
                    .toList();
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("category");
        mav.addObject("user", user);
        mav.addObject("category", category);
        mav.addObject("products", productsByCategory);
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }
}
