package app.web;

import app.email.service.EmailService;
import app.order.model.Order;
import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/profile/{username}")
    public ModelAndView profile(@PathVariable String username) {

        User user = userService.getByUsername(username);
        int cartQuantity = userService.getOrderQuantity(user);
        List<Order> orders = user.getOrders().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .toList();
        boolean isNotificationEnabled = emailService.getNotificationPreference(user.getId()).isEnabled();

        ModelAndView mav = new ModelAndView("user-profile");
        mav.addObject("user", user);
        mav.addObject("cartQuantity", cartQuantity);
        mav.addObject("orders", orders);
        mav.addObject("isNotificationEnabled", isNotificationEnabled);

        return mav;
    }

    @GetMapping("/profile/edit")
    public ModelAndView updateProfile(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        int cartQuantity = userService.getOrderQuantity(user);

        ModelAndView mav = new ModelAndView("edit-profile");
        mav.addObject("user", user);
        mav.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        mav.addObject("cartQuantity", cartQuantity);

        return mav;
    }

    @PutMapping("/profile/update")
    public ModelAndView updateProfile(@Valid UserEditRequest userEditRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("edit-profile");
            mav.addObject("userEditRequest", userEditRequest);
            mav.addObject("user", user);
            return mav;
        }

        userService.editUserProfile(user,userEditRequest);

        return new ModelAndView("redirect:/users/profile/"+user.getUsername());
    }

    @GetMapping("/notifications")
    public String updateNotifications(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());

        boolean isEnabled = emailService.getNotificationPreference(user.getId()).isEnabled();
        emailService.saveNotificationPreference(user.getId(),!isEnabled,user.getEmail());

        return "redirect:/users/profile/"+user.getUsername();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/toggle-status/{username}")
    public String changeStatus(@PathVariable String username){

        User user = userService.getByUsername(username);
        userService.changeStatus(user);

        return "redirect:/dashboard/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/toggle-ban/{username}")
    public String changeBan(@PathVariable String username){

        User user = userService.getByUsername(username);
        userService.changeBanFromSelling(user);

        return "redirect:/dashboard/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/toggle-role/{username}")
    public String changeRole(@PathVariable String username , @AuthenticationPrincipal AuthenticationDetails authenticationDetails){

        User currentUser = userService.getById(authenticationDetails.getUserId());
        User user = userService.getByUsername(username);
        boolean selfRoleChange = userService.changeRole(user,currentUser);

        if(selfRoleChange){
            return "redirect:/logout";
        }

        return "redirect:/dashboard/admin";
    }
}
