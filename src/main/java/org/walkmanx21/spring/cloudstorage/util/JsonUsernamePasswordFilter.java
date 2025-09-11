package org.walkmanx21.spring.cloudstorage.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.exceptions.InvalidCredentialsException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class JsonUsernamePasswordFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRequestDtoValidator userRequestDtoValidator;
    private final Validator hibernateValidator;

    @Autowired
    public JsonUsernamePasswordFilter(UserRequestDtoValidator userRequestDtoValidator, Validator hibernateValidator) {
        this.userRequestDtoValidator = userRequestDtoValidator;
        this.hibernateValidator = hibernateValidator;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            UserRequestDto userRequestDto = objectMapper.readValue(request.getInputStream(), UserRequestDto.class);

            var violations = hibernateValidator.validate(userRequestDto);
            if (!violations.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                violations.forEach(v -> builder.append(v.getMessage()).append("; "));
                throw new BadCredentialsException(builder.toString());
            }

            Errors errors = new BeanPropertyBindingResult(userRequestDto, "userRequestDto");
            userRequestDtoValidator.validate(userRequestDto, errors);

            if (errors.hasErrors()) {
                StringBuilder builder = new StringBuilder();
                List<FieldError> fieldErrors = errors.getFieldErrors();
                fieldErrors.forEach(error -> builder.append(error.getDefaultMessage()).append("; "));
                throw new BadCredentialsException(builder.toString());
            }

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword());
            setDetails(request, token);
            return this.getAuthenticationManager().authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
