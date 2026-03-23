package com.example.workshop06;

public class Plan {
    private String name;
    private String description;
    private String price;
    private String badge;

    public Plan(String name, String description, String price, String badge) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.badge = badge;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getBadge() {
        return badge;
    }
}