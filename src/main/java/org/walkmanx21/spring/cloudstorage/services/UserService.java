package org.walkmanx21.spring.cloudstorage.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.dto.UserResponseDto;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.models.UserRole;
import org.walkmanx21.spring.cloudstorage.repositories.UserRepository;
import org.walkmanx21.spring.cloudstorage.util.UserMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public UserResponseDto register(UserRequestDto userRequestDto) {
        User user = userMapper.convertToUser(userRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);
        storageService.createUserRootDirectory(user.getId());
        return userMapper.convertToUserResponseDto(user);
    }

    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new UserResponseDto(userDetails.getUsername());
    }
}
