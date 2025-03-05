package app.web;

import app.security.AuthenticationDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{username}")
    public ModelAndView profile(@PathVariable String username) {

        User user = userService.getByUsername(username);

        ModelAndView mav = new ModelAndView("user-profile");
        mav.addObject("user", user);

        return mav;
    }

    @GetMapping("/profile/edit")
    public ModelAndView updateProfile(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        System.out.println();
        User user = userService.getById(authenticationDetails.getUserId());

        ModelAndView mav = new ModelAndView("edit-profile");
        mav.addObject("user", user);
        mav.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

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
}
