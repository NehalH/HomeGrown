package com.example.efarm;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SellActivity extends AppCompatActivity {

    private ImageView productImageView;
    private EditText productNameEditText, quantityEditText, priceEditText;
    private RadioGroup categoryRadioGroup;
    private Button listProductButton;
    private Uri imageUri;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        productImageView = findViewById(R.id.productImageView);
        productNameEditText = findViewById(R.id.productNameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        categoryRadioGroup = findViewById(R.id.categoryRadioGroup);
        listProductButton = findViewById(R.id.listProductButton);

        // Set click listener for the list product button
        listProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the selected category
                int selectedCategoryId = categoryRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedCategoryRadioButton = findViewById(selectedCategoryId);
                String category = selectedCategoryRadioButton.getText().toString();

                // Retrieve other data
                String productName = productNameEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String price = priceEditText.getText().toString();
                String imageUrl = imageUri != null ? productImageView.toString() : "";

                // Upload data to Firebase Firestore
                uploadDataToFirestore(productName, category, quantity, price, imageUrl);
            }
        });
    }

    private void uploadDataToFirestore(String productName, String category, String quantity, String price, String imageUrl) {
        // Create a new document in the "products" collection
        DocumentReference documentRef = firestore.collection("products").document();

        // Create a data object to store the product details
        Product product = new Product(productName, category, quantity, price, imageUrl);

        // Upload the data to Firestore
        documentRef.set(product)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Data upload successful
                        // You can add any additional code or UI updates here
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred during data upload
                        // Handle the error appropriately
                    }
                });
    }
}
