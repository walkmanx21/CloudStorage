package org.walkmanx21.spring.cloudstorage.config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidCredentialsException;
import org.walkmanx21.spring.cloudstorage.util.JsonUsernamePasswordFilter;
import org.walkmanx21.spring.cloudstorage.util.UserRequestDtoValidator;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserRequestDtoValidator userRequestDtoValidator, Validator hibernateValidator) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAt(jsonUsernamePasswordFilter(authenticationManager, userRequestDtoValidator, hibernateValidator), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in", "/error").permitAll()
                        .anyRequest().hasAnyRole("USER", "ADMIN"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1));
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider (UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JsonUsernamePasswordFilter jsonUsernamePasswordFilter(AuthenticationManager authenticationManager, UserRequestDtoValidator userRequestDtoValidator, Validator hibernateValidator) {
        JsonUsernamePasswordFilter filter = new JsonUsernamePasswordFilter(userRequestDtoValidator, hibernateValidator);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            request.getSession(true);
            request.getSession().setAttribute(
                    "SPRING_SECURITY_CONTEXT",
                    SecurityContextHolder.getContext()
            );
            response.setContentType("application/json");
            response.getWriter().write("{\"username\":\"" + authentication.getName() + "\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        });
        filter.setAuthenticationFailureHandler((request, response, exception) -> {
                    response.setContentType("application/json");

                    String message = "{\"message\":\"Incorrect data (there is no such user, or the password is incorrect)\"}";
                    if (exception instanceof InvalidCredentialsException) {
                        message = "{\"message\":\"" + exception.getMessage() + "\"}";
                    }
                    response.getWriter().write(message);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
        );
        return filter;
    }
}

