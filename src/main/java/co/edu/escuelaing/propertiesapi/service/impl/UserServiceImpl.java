package co.edu.escuelaing.propertiesapi.service.impl;

import co.edu.escuelaing.propertiesapi.model.entity.User;
import co.edu.escuelaing.propertiesapi.repository.UserRepository;
import co.edu.escuelaing.propertiesapi.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(String username, String password, String role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // BCrypt hash
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);
    }

    private void createDefaultAdminUser() {
        if (!findByUsername("admin").isPresent()) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("juanito"))
                    .role("ADMIN")
                    .enabled(true)
                    .build();
            userRepository.save(adminUser);
            System.out.println("Admin user created");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        createDefaultAdminUser();
    }
}
