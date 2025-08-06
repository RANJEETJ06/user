package com.service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionRequest {
    private Long userId;
    private Long documentId;
    private String role;
}