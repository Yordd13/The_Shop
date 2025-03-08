package app.web;

import app.category.model.Category;
import app.category.service.CategoryService;
import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.NewProductRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderItemService orderItemService;

    @Autowired
    public ProductController(UserService userService, ProductService productService, CategoryService categoryService, OrderItemService orderItemService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderItemService = orderItemService;
    }

    @GetMapping("/{productId}")
    public ModelAndView productPage(@PathVariable("productId") UUID productId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);

        Product product = productService.getById(productId);

        ModelAndView mav = new ModelAndView("product");
        mav.addObject("user", user);
        mav.addObject("product", product);
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }

    @PostMapping("/{productId}/add")
    public String addProduct(@PathVariable("productId") UUID productId,@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        Product product = productService.getById(productId);

        orderItemService.addNewOrderItem(user,product);

        return "redirect:/products/" + productId;
    }

    @GetMapping("/add")
    public ModelAndView addProductPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        List<Category> categories = categoryService.getAllCategories();
        int cartQuantity = userService.getOrderQuantity(user);

        ModelAndView mav = new ModelAndView("new-product");
        mav.addObject("user", user);
        mav.addObject("categories", categories);
        mav.addObject("newProductRequest",new NewProductRequest());
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }

    @PostMapping
    public ModelAndView addNewProduct(@Valid NewProductRequest newProductRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());

        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            int cartQuantity = userService.getOrderQuantity(user);

            ModelAndView mav = new ModelAndView("new-product");
            mav.addObject("user", user);
            mav.addObject("categories", categories);
            mav.addObject("newProductRequest", newProductRequest);
            mav.addObject("cartQuantity", cartQuantity);
            return mav;
        }


        // Add new product
        productService.addNewProduct(newProductRequest, user);
        userService.addSellerRole(user);

        return new ModelAndView("redirect:/categories");
    }
}

