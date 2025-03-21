package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
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
}
