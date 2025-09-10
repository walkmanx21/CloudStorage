package org.walkmanx21.spring.cloudstorage.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.models.User;

@Component
public class UserMapper {

    private ModelMapper mapper = new ModelMapper();

    public User convertToUser (UserRequestDto userRequestDto) {
        return mapper.map(userRequestDto, User.class);
    }

    public UserResponseDto convertToUserResponseDto(User user) {
        return mapper.map(user, UserResponseDto.class);
    }

}
