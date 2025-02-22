package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
@RequestMapping("/")
public class IndexController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public IndexController(UserService userService, ProductService productService, CategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getBaseHome() {
        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegister() {

        ModelAndView modelAndView = new ModelAndView("register");

        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public String postRegister(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerRequest);

        return "redirect:/login";
    }



    @GetMapping("/login")
    public ModelAndView getLogin(@RequestParam(value = "error", required = false) String error) {

        ModelAndView modelAndView = new ModelAndView("login");

        if(error != null) {
            modelAndView.addObject("errorMessage", "Incorrect email or password!");
        }

        return modelAndView;
    }


    @GetMapping("/categories")
    public ModelAndView getCategoryPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        List<Category> categories = categoryService.getAllCategories();

        ModelAndView modelAndView = new ModelAndView("categories");
        modelAndView.addObject("user", user);
        modelAndView.addObject("categoryList",categories);

        return modelAndView;
    }
}
