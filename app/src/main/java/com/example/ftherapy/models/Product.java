package com.example.ftherapy.models;

public class Product {
    private String name;
    private int imageRes;
    private String description; // המידע הייחודי החדש

    // עדכון הבנאי (Constructor) שיקבל גם תיאור
    public Product(String name, int imageRes, String description) {
        this.name = name;
        this.imageRes = imageRes;
        this.description = description;
    }

    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
    public String getDescription() { return description; } // גטר לתיאור
}