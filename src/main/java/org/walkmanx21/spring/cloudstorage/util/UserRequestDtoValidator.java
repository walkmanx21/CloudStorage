package org.walkmanx21.spring.cloudstorage.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;

import java.util.List;

@Component
public class UserRequestDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRequestDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        UserRequestDto userRequestDto = (UserRequestDto) target;
        if (userRequestDto.getUsername().equals(userRequestDto.getPassword()))
            errors.rejectValue("password", "", "Password and username must not match");

        if (checkPasswordInBlacklist(userRequestDto.getPassword())) {
            errors.rejectValue("password", "", "Password is too simple. Come up with a more complex password");
        }

    }

    private boolean checkPasswordInBlacklist(String password) {
        List<String> blackList = List.of("123456", "1234567", "12345678", "123456789", "qwerty", "password", "admin");
        return blackList.contains(password);
    }
}
