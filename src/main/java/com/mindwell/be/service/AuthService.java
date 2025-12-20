package com.mindwell.be.service;

import com.mindwell.be.dto.auth.AuthResponse;
import com.mindwell.be.dto.auth.LoginRequest;
import com.mindwell.be.dto.auth.RegisterRequest;
import com.mindwell.be.entity.User;
import com.mindwell.be.repository.UserRepository;
import com.mindwell.be.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName().trim())
                .phoneNumber(req.phoneNumber())
                .mindpointsBalance(0)
                .memberSince(LocalDate.now())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getUserId(), user.getEmail());
        return new AuthResponse("Bearer", token, user.getUserId(), user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String token = jwtService.generateAccessToken(user.getUserId(), user.getEmail());
        return new AuthResponse("Bearer", token, user.getUserId(), user.getEmail(), user.getFullName());
    }
}
