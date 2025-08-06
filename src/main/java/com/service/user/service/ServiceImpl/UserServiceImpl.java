package com.service.user.service.ServiceImpl;

import com.service.user.entity.CustomOAuth2User;
import com.service.user.entity.Role;
import com.service.user.entity.User;
import com.service.user.entity.UserDocumentPermission;
import com.service.user.exception.ResourceNotFoundException;
import com.service.user.repository.RoleRepository;
import com.service.user.repository.UserRepository;
import com.service.user.repository.UserDocumentPermissionRepository;
import com.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends DefaultOAuth2UserService implements UserService, UserDetailsService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserDocumentPermissionRepository permissionRepository;
    @Autowired
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String googleSub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name");

        if (googleSub == null || email == null) {
            throw new IllegalArgumentException("Invalid OAuth2 user data");
        }

        User user = userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> registerOAuth2User(googleSub, username, email));
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    @Override
    @Transactional
    public User registerOAuth2User(String googleSub, String username, String email) {
        User user = new User();
        user.setGoogleSub(googleSub);
        user.setUsername(username != null ? username : email.split("@")[0]);
        user.setEmail(email);

        List<Role> roles = new ArrayList<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("USER role not found"));
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.saveAndFlush(user);

        // Log for debugging
        System.out.println("User saved with ID: " + savedUser.getId());

        return savedUser;
    }

    @Override
    public User findByGoogleSub(String googleSub) {
        return userRepository.findByGoogleSub(googleSub)
                .orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Mail with "+email));
    }

    @Override
    public UserDocumentPermission addDocumentPermission(Long userId, Long documentId, String role) {
        UserDocumentPermission permission = new UserDocumentPermission();
        permission.setUserId(userId);
        permission.setDocumentId(documentId);
        permission.setRole(role);
        return permissionRepository.save(permission);
    }

    @Override
    public String getDocumentRole(Long userId, Long documentId) {
        return permissionRepository.findByUserIdAndDocumentId(userId, documentId)
                .map(UserDocumentPermission::getRole)
                .orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return new CustomOAuth2User(user, Collections.emptyMap());
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByEmailOptional(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElse(null); // Return null instead of throwing exception
    }

}