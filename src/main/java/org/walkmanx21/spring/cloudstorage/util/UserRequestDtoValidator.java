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
            errors.rejectValue("password", "", "Имя пользователя и пароль не должны совпадать");

        if (checkPasswordInBlacklist(userRequestDto.getPassword())) {
            errors.rejectValue("password", "", "Пароль слишком простой. Придумайте более сложный пароль");
        }

    }

    private boolean checkPasswordInBlacklist(String password) {
        List<String> blackList = List.of("123456", "1234567", "12345678", "123456789", "qwerty", "password", "admin");
        return blackList.contains(password);
    }
}
