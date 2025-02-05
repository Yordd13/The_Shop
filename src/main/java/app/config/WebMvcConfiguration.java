package app.config;

import app.security.SessionCheckInterception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SessionCheckInterception sessionCheckInterception;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(sessionCheckInterception)
        .addPathPatterns("/**");

        //TODO .excludePathPatterns("/css/**","/js/**","/images/**","/fonts/**"); ili kakvoto imam posle vuv static
    }
}
