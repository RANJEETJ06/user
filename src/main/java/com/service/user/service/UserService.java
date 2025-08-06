package com.service.user.service;

import com.service.user.entity.User;
import com.service.user.entity.UserDocumentPermission;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User>, UserDetailsService {
    OAuth2User loadUser(OAuth2UserRequest userRequest);
    User registerOAuth2User(String googleSub, String username, String email);
    User findByGoogleSub(String googleSub);
    User findByEmail(String email);
    UserDocumentPermission addDocumentPermission(Long userId, Long documentId, String role);
    String getDocumentRole(Long userId, Long documentId);
    UserDetails loadUserByUsername(String username);
    User saveUser(User user);
    User findByEmailOptional(String email);
}
