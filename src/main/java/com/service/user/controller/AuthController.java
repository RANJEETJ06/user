package com.service.user.controller;

import com.service.user.entity.Role;
import com.service.user.entity.User;
import com.service.user.configs.JwtUtil;
import com.service.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.stream.Collectors;

// Update your AuthController to redirect to home after registration

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/oauth2/login")
    public void oauth2LoginSuccess(@AuthenticationPrincipal OAuth2User oauth2User,
                                   HttpServletResponse response) throws IOException {

        try {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String googleSub = oauth2User.getAttribute("sub");

            System.out.println("Processing OAuth2 user: " + email);

            // Check if user exists by googleSub first, then by email
            User user = userService.findByGoogleSub(googleSub);

            if (user == null) {
                // Try to find by email as fallback
                user = userService.findByEmailOptional(email);

                if (user == null) {
                    // Register new user
                    System.out.println("Registering new user...");
                    userService.registerOAuth2User(googleSub, name, email);
                } else {
                    // Update existing user with Google sub if found by email
                    user.setGoogleSub(googleSub);
                    userService.saveUser(user);
                }
            } else {
                System.out.println("Existing user found: " + user.getEmail());
            }

            // After successful registration/login, redirect to home
            response.sendRedirect("/api/users/me");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/api/auth/error?message=" + e.getMessage());
        }
    }

    // Optional: Add endpoint to return JWT token instead of redirect
    @GetMapping("/oauth2/token")
    public ResponseEntity<String> getToken(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            String email = oauth2User.getAttribute("email");
            User user = userService.findByEmailOptional(email);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            String roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(","));

            String token = jwtUtil.generateToken(user.getUsername(), roles);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}