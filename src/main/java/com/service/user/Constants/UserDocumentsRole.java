package com.service.user.Constants;

import java.util.List;

public final class UserDocumentsRole {

    private UserDocumentsRole() {
        // private constructor to prevent instantiation
    }

    public static final List<String> DOCUMENT_ROLES = List.of("OWNER", "EDITOR", "VIEWER");
}
