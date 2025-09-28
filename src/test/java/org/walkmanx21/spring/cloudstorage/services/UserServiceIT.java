package org.walkmanx21.spring.cloudstorage.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.walkmanx21.spring.cloudstorage.dto.UserRequestDto;
import org.walkmanx21.spring.cloudstorage.models.User;
import org.walkmanx21.spring.cloudstorage.repositories.UserRepository;

import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceIT {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres");

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceIT(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @BeforeAll
    static void runContainer() {
        container.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
    }

    @Test
    public void whenUserRequestDtoComeThanRegister() {
        UserRequestDto userRequestDto = new UserRequestDto("testUser123", "testPassword123");

        userService.register(userRequestDto);
        Optional<User> mayBeUser = userRepository.findUserByUsername(userRequestDto.getUsername());

        Assertions.assertEquals(1, userRepository.findAll().size());
        Assertions.assertEquals(userRequestDto.getUsername(), mayBeUser.map(User::getUsername).orElse(null));
    }
}
