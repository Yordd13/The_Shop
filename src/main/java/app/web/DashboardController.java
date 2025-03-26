package app.web;

import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UpdateQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    @Autowired
    public DashboardController(UserService userService, OrderItemService orderItemService, ProductService productService) {
        this.userService = userService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admin")
    public ModelAndView getAdminPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails){

        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);
        List<User> users = userService.getAllUsers();

        ModelAndView mav = new ModelAndView("admin-control-panel");
        mav.addObject("user", user);
        mav.addObject("cartQuantity", cartQuantity);
        mav.addObject("users", users);

        return mav;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/users/toggle-status/{username}")
    public String changeStatus(@PathVariable String username){

        User user = userService.getByUsername(username);
        userService.changeStatus(user);

        return "redirect:/dashboard/admin";
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/users/toggle-ban/{username}")
    public String changeBan(@PathVariable String username){

        User user = userService.getByUsername(username);
        userService.changeBanFromSelling(user);

        return "redirect:/dashboard/admin";
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/users/toggle-role/{username}")
    public String changeRole(@PathVariable String username){

        User user = userService.getByUsername(username);
        userService.changeRole(user);

        return "redirect:/dashboard/admin";
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/seller")
    public ModelAndView getSellerPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails){
        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);

        int salesLast24hours = orderItemService.getSales(user, LocalDateTime.now().minusHours(24));
        int salesLastMonth = orderItemService.getSales(user, LocalDateTime.now().minusMonths(1));
        int salesLastYear = orderItemService.getSales(user, LocalDateTime.now().minusYears(1));

        BigDecimal profitLast24hours = orderItemService.getProfit(user, LocalDateTime.now().minusHours(24))
                .multiply(new BigDecimal("0.95"));
        BigDecimal profitLastMonth = orderItemService.getProfit(user,LocalDateTime.now().minusMonths(1))
                .multiply(new BigDecimal("0.95"));
        BigDecimal profitLastYear = orderItemService.getProfit(user,LocalDateTime.now().minusYears(1))
                .multiply(new BigDecimal("0.95"));

        List<Product> products = user.getProductsForSale().stream()
                .sorted(Comparator.comparingInt(product -> product.getQuantity() == 0 ? 0 : 1))
                .toList();

        ModelAndView mav = new ModelAndView("seller-dashboard");
        mav.addObject("user", user);
        mav.addObject("cartQuantity", cartQuantity);
        mav.addObject("salesLast24hours", salesLast24hours);
        mav.addObject("salesLastMonth", salesLastMonth);
        mav.addObject("salesLastYear", salesLastYear);
        mav.addObject("profitLast24hours", profitLast24hours);
        mav.addObject("profitLastMonth", profitLastMonth);
        mav.addObject("profitLastYear", profitLastYear);
        mav.addObject("products", products);

        return mav;
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
    @GetMapping("/discontinue/{productId}")
    public String discontinue(@PathVariable UUID productId) {
        Product product = productService.getById(productId);

        productService.discontinueProduct(product);

        return "redirect:/dashboard/seller";
    }
}
