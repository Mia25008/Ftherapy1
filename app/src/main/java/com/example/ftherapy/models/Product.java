package com.example.ftherapy.models;

public class Product {
    private String name;
    private int imageRes;

    public Product(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
}
