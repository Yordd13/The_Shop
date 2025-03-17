package app.web;

import app.exception.RegistrationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(RegistrationException.class)
    public String handleRegistrationException(RegistrationException ex, RedirectAttributes redirectAttributes) {
        for (String errorMessage : ex.getErrorMessages()) {
            if (errorMessage.contains("Username")) {
                redirectAttributes.addFlashAttribute("usernameExistsExceptionMessage", errorMessage);
            } else if (errorMessage.contains("Email")) {
                redirectAttributes.addFlashAttribute("emailExistsExceptionMessage", errorMessage);
            }
        }
        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundException() {

        return new ModelAndView("not-found-error");
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {
        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
