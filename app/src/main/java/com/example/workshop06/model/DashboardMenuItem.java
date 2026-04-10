package com.example.workshop06.model;

public class DashboardMenuItem {
    private final String icon;
    private final String title;
    private final String subtitle;
    private final int backgroundResId;
    private final int iconBackgroundResId;
    private final Class<?> targetActivity;
    private String extraKey;
    private String extraValue;

    public DashboardMenuItem(String icon,
                             String title,
                             String subtitle,
                             int backgroundResId,
                             int iconBackgroundResId,
                             Class<?> targetActivity) {
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.backgroundResId = backgroundResId;
        this.iconBackgroundResId = iconBackgroundResId;
        this.targetActivity = targetActivity;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }

    public int getIconBackgroundResId() {
        return iconBackgroundResId;
    }

    public Class<?> getTargetActivity() {
        return targetActivity;
    }

    public void setExtra(String key, String value) {
        this.extraKey = key;
        this.extraValue = value;
    }

    public String getExtraKey() {
        return extraKey;
    }

    public String getExtraValue() {
        return extraValue;
    }
}