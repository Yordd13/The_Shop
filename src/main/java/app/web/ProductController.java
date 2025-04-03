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
import app.web.dto.UpdateQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
        Authentication newAuth = authenticationDetails.update(user);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return new ModelAndView("redirect:/categories");
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/update/{productId}")
    public ModelAndView showUpdateForm(@PathVariable UUID productId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        ModelAndView mav = new ModelAndView("update-quantity");
        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);
        Product product = productService.getById(productId);

        mav.addObject("user", user);
        mav.addObject("cartQuantity", cartQuantity);
        mav.addObject("product", product);
        mav.addObject("UpdateQuantityRequest",new UpdateQuantityRequest());

        return mav;
    }


    @PreAuthorize("hasAnyRole('SELLER')")
    @PutMapping("/update/{productId}")
    public ModelAndView updateQuantity(@PathVariable UUID productId, @Valid UpdateQuantityRequest UpdateQuantityRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Product product = productService.getById(productId);

        if(bindingResult.hasErrors()) {
            int cartQuantity = userService.getOrderQuantity(user);

            ModelAndView mav = new ModelAndView("update-quantity");
            mav.addObject("user", user);
            mav.addObject("cartQuantity", cartQuantity);
            mav.addObject("product", product);
            mav.addObject("UpdateQuantityRequest", UpdateQuantityRequest);

            return mav;
        }

        //update the product
        productService.updateQuantity(product,UpdateQuantityRequest);

        return new ModelAndView("redirect:/dashboard/seller");
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/remove/{productId}")
    public String removeProduct(@PathVariable UUID productId) {
        Product product = productService.getById(productId);

        productService.removeProduct(product);

        return "redirect:/dashboard/seller";
    }
}

