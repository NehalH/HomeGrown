package com.example.efarm;

public class Product {
    private String productName;
    private String category;
    private String quantity;
    private String price;
    private String imageUrl;

    public Product() {
        // Empty constructor required for Firestore
    }

    public Product(String productName, String category, String quantity, String price, String imageUrl) {
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
