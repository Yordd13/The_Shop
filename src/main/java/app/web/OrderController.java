package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.orderItem.model.OrderItem;
import app.orderItem.service.OrderItemService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final UserService userService;
    private final OrderItemService orderItemService;
    private final OrderService orderService;

    @Autowired
    public OrderController(UserService userService, OrderItemService orderItemService, OrderService orderService) {
        this.userService = userService;
        this.orderItemService = orderItemService;
        this.orderService = orderService;
    }

    @GetMapping
    public ModelAndView order(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        List<OrderItem> orderItems = user.getOrderItems().stream()
                .filter(orderItem -> orderItem.getOrder() == null)
                .collect(Collectors.toList());

        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);
        int cartQuantity = userService.getOrderQuantity(user);

        ModelAndView mav = new ModelAndView("order");
        mav.addObject("user", user);
        mav.addObject("orderItems", orderItems);
        mav.addObject("totalPrice", totalPrice);
        mav.addObject("cartQuantity", cartQuantity);

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

    @GetMapping("/purchase")
    public String purchaseOrder(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        List<OrderItem> orderItems = user.getOrderItems().stream()
                .filter(orderItem -> orderItem.getOrder() == null)
                .collect(Collectors.toList());

        BigDecimal totalPrice = orderItemService.getTotalPrice(orderItems);

        //make an order
        orderService.makeAnOrder(orderItems,user,totalPrice);

        return "redirect:/categories";
    }

    @GetMapping("/{orderItemId}/remove")
    public String removeOrderItem(@PathVariable UUID orderItemId){

        orderItemService.deleteOrderItem(orderItemId);

        return "redirect:/order";
    }

    @GetMapping("/{orderId}/preview")
    public ModelAndView orderPreview(@PathVariable UUID orderId, @AuthenticationPrincipal AuthenticationDetails authenticationDetails){
        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);
        Order order = orderService.getById(orderId);

        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order);

        ModelAndView mav = new ModelAndView("order-preview");
        mav.addObject("user", user);
        mav.addObject("orderItems", orderItems);
        mav.addObject("order", order);
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }
}
