package com.example.efarm;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class ProductDetailsActivity extends AppCompatActivity {
    private TextView productNameTextView;
    private TextView categoryTextView;
    private TextView quantityTextView;
    private TextView priceTextView;
    private TextView sellerTextView;
    private TextView phoneTextView;
    private TextView addressTextView;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Find the TextViews by their IDs
        productNameTextView = findViewById(R.id.productnametextView);
        categoryTextView = findViewById(R.id.categorytextView);
        quantityTextView = findViewById(R.id.emailtextView);
        priceTextView = findViewById(R.id.pricetextView);
        sellerTextView = findViewById(R.id.sellertextView);
        phoneTextView = findViewById(R.id.phonetextView);
        addressTextView = findViewById(R.id.addresstextView);

        // Get the product ID from the intent extras
        String productId = getIntent().getStringExtra("productId");

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        // Retrieve the product details from Firestore
        firestore.collection("products").document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the product details
                            String productName = document.getString("productName");
                            String category = document.getString("category");
                            String quantity = document.getString("quantity");
                            String price = document.getString("price");
                            String seller = document.getString("seller");
                            String phone = document.getString("phone");
                            String address = document.getString("address");

                            // Set the product details to the respective TextViews
                            productNameTextView.setText(productName);
                            categoryTextView.setText(category);
                            quantityTextView.setText(quantity);
                            priceTextView.setText(price);
                            sellerTextView.setText(seller);
                            phoneTextView.setText(phone);
                            addressTextView.setText(address);
                        } else {
                            // Document does not exist
                        }
                    } else {
                        // Error fetching document
                        FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
                        if (exception != null) {
                            // Handle the exception
                        }
                    }
                });
    }
}
