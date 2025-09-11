package org.walkmanx21.spring.cloudstorage.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.UserRole;
import org.walkmanx21.spring.cloudstorage.repositories.UserRepository;
import org.walkmanx21.spring.cloudstorage.security.MyUserDetails;
import org.walkmanx21.spring.cloudstorage.util.UserMapper;

@Service
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public UserService(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authManager) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
    }

    public UserResponseDto register(UserRequestDto userRequestDto) {
        User user = userMapper.convertToUser(userRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);
        return userMapper.convertToUserResponseDto(user);
    }

    public UserResponseDto authorize(UserRequestDto userRequestDto) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword()));
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        return new UserResponseDto(userDetails.getUsername());
    }
}
