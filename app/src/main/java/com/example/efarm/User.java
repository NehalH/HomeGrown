package com.example.efarm;

import com.google.firebase.firestore.Exclude;

public class User {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String imageUrl;

    public User() {
        // Default constructor required for Firebase Firestore
    }

    public User(String name, String phone, String email, String address, String imageUrl) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Exclude the setter for imageUrl from serialization to Firestore
    @Exclude
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
