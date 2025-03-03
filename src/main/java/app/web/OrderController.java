package app.web;

import app.orderItem.model.OrderItem;
import app.products.service.ProductService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public OrderController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping
    public ModelAndView order(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<OrderItem> orderItems = user.getOrderItems();

        ModelAndView mav = new ModelAndView("order");
        mav.addObject("user", user);
        mav.addObject("orderItems", orderItems);

        return mav;
    }
}
