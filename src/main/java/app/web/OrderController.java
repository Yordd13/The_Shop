package app.web;

import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
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
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Autowired
    public OrderController(UserService userService, ProductService productService, OrderItemService orderItemService) {
        this.userService = userService;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public ModelAndView order(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<OrderItem> orderItems = user.getOrderItems();
        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);

        ModelAndView mav = new ModelAndView("order");
        mav.addObject("user", user);
        mav.addObject("orderItems", orderItems);
        mav.addObject("totalPrice", totalPrice);

        return mav;
    }

    @GetMapping("/{orderItemId}/increase")
    public String increaseQuantity(@PathVariable UUID orderItemId){

        orderItemService.increaseQuantity(orderItemId);

        return "redirect:/order";
    }

    @GetMapping("/{orderItemId}/decrease")
    public String decreaseQuantity(@PathVariable UUID orderItemId){

        orderItemService.decreaseQuantity(orderItemId);

        return "redirect:/order";
    }
}
