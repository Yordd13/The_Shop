package app.security;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionCheckInterception implements HandlerInterceptor {

    //This endpoints don't need sessions
    private final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/","/login","/register");

    private final UserService userService;

    @Autowired
    public SessionCheckInterception(UserService userService) {
        this.userService = userService;
    }

    //This method will work after every request from the user
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String endpoint = request.getServletPath();

        if(UNAUTHENTICATED_ENDPOINTS.contains(endpoint)){
            return true;
        }

        HttpSession session = request.getSession(false);
        if(session == null){
            response.sendRedirect("/login");
            return false;
        }


        UUID userId = (UUID) session.getAttribute("user_id");

        User user = userService.getById(userId);

        if(!user.isActive()){
            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}
