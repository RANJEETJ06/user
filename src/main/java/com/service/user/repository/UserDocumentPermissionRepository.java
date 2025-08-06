package com.service.user.repository;

import com.service.user.entity.UserDocumentPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentPermissionRepository extends JpaRepository<UserDocumentPermission, Long> {
    Optional<UserDocumentPermission> findByUserIdAndDocumentId(Long userId, Long documentId);
}
