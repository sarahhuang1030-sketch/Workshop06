package com.example.workshop06.model;

public class ActivityLogResponse {
    private Long id;
    private String module;
    private String action;
    private String target;
    private String doneBy;
    private String timestamp;

    public Long getId() {
        return id;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getTarget() {
        return target;
    }

    public String getDoneBy() {
        return doneBy;
    }

    public String getTimestamp() {
        return timestamp;
    }
}