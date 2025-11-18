package com.example.kolla.enums;

public enum MeetingRole {
    HOST("Host", "Meeting host with full control"),
    CO_HOST("Co-Host", "Meeting co-host with most control permissions"),
    SECRETARY("Secretary", "Meeting secretary with note-taking and documentation responsibilities"),
    PRESENTER("Presenter", "Can present content in the meeting"),
    PARTICIPANT("Participant", "Regular meeting participant"),
    OBSERVER("Observer", "Can only observe the meeting");

    private final String displayName;
    private final String description;

    MeetingRole(String displayName, String description) {
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