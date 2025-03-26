package app.web;

import app.orderItem.service.OrderItemService;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final OrderItemService orderItemService;

    @Autowired
    public DashboardController(UserService userService, OrderItemService orderItemService) {
        this.userService = userService;
        this.orderItemService = orderItemService;
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

        BigDecimal profitLast24hours = orderItemService.getProfit(user, LocalDateTime.now().minusHours(24));
        BigDecimal profitLastMonth = orderItemService.getProfit(user,LocalDateTime.now().minusMonths(1));
        BigDecimal profitLastYear = orderItemService.getProfit(user,LocalDateTime.now().minusYears(1));

        ModelAndView mav = new ModelAndView("seller-dashboard");
        mav.addObject("user", user);
        mav.addObject("cartQuantity", cartQuantity);
        mav.addObject("salesLast24hours", salesLast24hours);
        mav.addObject("salesLastMonth", salesLastMonth);
        mav.addObject("salesLastYear", salesLastYear);
        mav.addObject("profitLast24hours", profitLast24hours);
        mav.addObject("profitLastMonth", profitLastMonth);
        mav.addObject("profitLastYear", profitLastYear);

        return mav;
    }
}
