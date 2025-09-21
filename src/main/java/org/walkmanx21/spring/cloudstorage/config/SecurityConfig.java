package org.walkmanx21.spring.cloudstorage.config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidRequestDataException;
import org.walkmanx21.spring.cloudstorage.util.JsonUsernamePasswordFilter;
import org.walkmanx21.spring.cloudstorage.util.UserRequestDtoValidator;

import java.util.List;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, UserRequestDtoValidator userRequestDtoValidator, Validator hibernateValidator) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )
                .addFilterAt(jsonUsernamePasswordFilter(authenticationManager, hibernateValidator), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in", "/").permitAll()
                        .requestMatchers("/api/user/me").authenticated()
                        .anyRequest().hasAnyRole("USER", "ADMIN"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) ->
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
                        .logoutSuccessHandler((request, response, authentication) -> {
                                    if (authentication == null) {
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    } else {
                                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    }
                                }
                        )
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION"));
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JsonUsernamePasswordFilter jsonUsernamePasswordFilter(AuthenticationManager authenticationManager, Validator hibernateValidator) {
        JsonUsernamePasswordFilter filter = new JsonUsernamePasswordFilter(hibernateValidator);
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
                    if (exception instanceof InvalidRequestDataException) {
                        message = "{\"message\":\"" + exception.getMessage() + "\"}";
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                    response.getWriter().write(message);
                }
        );
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8082", "http://host.docker.internal:8082")); //TODO указать потом реальный адрес приложения
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}

