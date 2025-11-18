package com.example.kolla.enums;

public enum Role {
    ADMIN("Administrator", "Full system access"),
    SECRETARY("Secretary", "Meeting management and documentation access"),
    USER("User", "Basic user access");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}