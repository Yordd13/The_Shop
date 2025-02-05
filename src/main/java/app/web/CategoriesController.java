package app.web;

import app.category.service.CategoryService;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/categories")
public class CategoriesController {
    private final UserService userService;
    private final CategoryService categoryService;

    @Autowired
    public CategoriesController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ModelAndView getHome(HttpSession session) {

        UUID id = (UUID) session.getAttribute("user_id");
        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView("categories");
        modelAndView.addObject("user", user);
        modelAndView.addObject("categoryList",categoryService.getAllCategories());

        return modelAndView;
    }
}
