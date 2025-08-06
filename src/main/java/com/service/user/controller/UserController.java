package com.service.user.controller;

import com.service.user.dto.PermissionRequest;
import com.service.user.entity.User;
import com.service.user.entity.UserDocumentPermission;
import com.service.user.exception.ResourceNotFoundException;
import com.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }


    @PostMapping("/permissions")
    public ResponseEntity<?> addDocumentPermission(
            @RequestBody PermissionRequest request,
            @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        try {
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(400).body("Email not found in OAuth2 profile");
            }
            User currentUser = userService.findByEmail(email);
            // Verify current user has permission to assign roles (e.g., OWNER or ADMIN)
            String currentUserRole = userService.getDocumentRole(currentUser.getId(), request.getDocumentId());
            if (!"OWNER".equals(currentUserRole) && !"ADMIN".equals(currentUserRole)) {
                return ResponseEntity.status(403).body("Not authorized to assign permissions");
            }
            UserDocumentPermission permission = userService.addDocumentPermission(
                    request.getUserId(),
                    request.getDocumentId(),
                    request.getRole()
            );
            return ResponseEntity.ok(permission);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to assign permission: " + e.getMessage());
        }
    }

    @GetMapping("/permissions/{documentId}")
    public ResponseEntity<?> getDocumentRole(
            @PathVariable Long documentId,
            @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        try {
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(400).body("Email not found in OAuth2 profile");
            }
            User user = userService.findByEmail(email);
            String role = userService.getDocumentRole(user.getId(), documentId);
            if (role == null) {
                return ResponseEntity.status(404).body("No permission found for document");
            }
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve document role: " + e.getMessage());
        }
    }
}

