package com.example.kolla.enums;

public enum NotificationType {
    MEETING_CREATED("Meeting Created", "A new meeting has been created"),
    MEETING_UPDATED("Meeting Updated", "Meeting details have been updated"),
    MEETING_CANCELLED("Meeting Cancelled", "A meeting has been cancelled"),
    MEETING_REMINDER("Meeting Reminder", "Reminder for upcoming meeting"),
    DOCUMENT_SHARED("Document Shared", "A document has been shared"),
    ROLE_CHANGED("Role Changed", "Your role has been changed"),
    GENERAL_ANNOUNCEMENT("Announcement", "General announcement");
    
    private final String displayName;
    private final String description;

    NotificationType(String displayName, String description) {
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
