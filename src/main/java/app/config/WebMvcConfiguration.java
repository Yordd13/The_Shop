package app.config;

import app.security.SessionCheckInterception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        //requestMatchers - достъп по даден endpoint
        http
                .authorizeHttpRequests(matchers -> matchers
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/","/register").permitAll()
                .anyRequest().authenticated()
        )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/categories")
                        .failureUrl("/login?error")
                        .permitAll()
                )
        ;

        //.requestMatchers("/users").hasRole("ADMIN")

        return http.build();
    }
}
