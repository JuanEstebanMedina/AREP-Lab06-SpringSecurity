package co.edu.escuelaing.propertiesapi.service;

import co.edu.escuelaing.propertiesapi.model.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    void saveUser(String username, String password, String role);
}