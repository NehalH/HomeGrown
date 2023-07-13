package com.example.efarm;

public class Product {
    private String productName;
    private String category;
    private String productID;
    private String imageFileName;
    private double quantity;
    private double price;
    private String imageUrl;
    private String userID;

    public Product() {
        // Empty constructor required for Firestore
    }

    public Product(String productName, String category, double quantity, double price, String imageUrl, String userID, String productId) {
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.userID = userID;
        this.productID = productId;
    }

    public Product(String mango, String sweet_mango, double v) {
    }

    // Getter and Setter for productName
    public String getProductName() {
        return productName;
    }
    public String getProductID() {
        return productID;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Getter and Setter for category
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getter and Setter for quantity
    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    // Getter and Setter for price
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Getter and Setter for imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter and Setter for userID
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDescription() {
        // Implement your logic to retrieve the description for the product
        return "Product description";
    }

    public void setDescription(String description) {
        // Implement your logic to set the description for the product
        // For example:
        // this.description = description;
    }
    public String getImageFileName() {
        return imageFileName;
    }
}
