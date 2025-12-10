package com.example.bank.service;

import com.example.bank.model.AppUser;
import com.example.bank.repository.AppUserRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    AppUserRepository repo;

    @Transactional
    public AppUser register(String username, String rawPassword, String fullName, String email, String phone, String address) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank() || fullName == null || fullName.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Username, password, full name and email are required");
        }
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        AppUser u = AppUser.builder()
                .username(username)
                .passwordHash(hash)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .address(address)
                .enabled(true)
                .build();
        return repo.save(u);

    }

    public AppUser authenticate(String username, String rawPassword) {
        return repo.findByUsername(username)
                .filter(u -> u.isEnabled() && BCrypt.checkpw(rawPassword, u.getPasswordHash()))
                .orElse(null);
    }
} 