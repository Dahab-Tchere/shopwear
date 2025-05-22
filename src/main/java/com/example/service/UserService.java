package com.example.service;

import com.example.dto.UserRegistrationDTO;
import com.example.dto.UserProfileDTO;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String ADMIN_CODE = "NOVA_ADMIN_2025"; // Hardcoded for simplicity

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(@Valid UserRegistrationDTO userDTO, String adminCode) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists: " + userDTO.getUsername());
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + userDTO.getEmail());
        }
        if ("ROLE_ADMIN".equals(userDTO.getRole()) && !ADMIN_CODE.equals(adminCode)) {
            throw new IllegalStateException("Invalid admin registration code");
        }
        User user = new User(
                userDTO.getUsername(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getFullName(),
                userDTO.getRole()
        );
        return userRepository.save(user);
    }

    public User updateProfile(String username, @Valid UserProfileDTO profileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (!user.getEmail().equals(profileDTO.getEmail()) &&
                userRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + profileDTO.getEmail());
        }
        user.setFullName(profileDTO.getFullName());
        user.setEmail(profileDTO.getEmail());
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}