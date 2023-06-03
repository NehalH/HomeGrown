package com.example.efarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class SellActivity extends AppCompatActivity {
    private EditText productNameEditText, priceEditText, quantityEditText;
    private Spinner categorySpinner;
    private Button submitButton;
    private TextView sellerInfoTextView;

    private FirebaseFirestore db;
    private CollectionReference productsCollection;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // Initialize views
        productNameEditText = findViewById(R.id.productNameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        submitButton = findViewById(R.id.submitButton);
        sellerInfoTextView = findViewById(R.id.sellerInfoTextView);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        productsCollection = db.collection("products");

        // Initialize Firebase Cloud Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("product_images");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the input values
                String productName = productNameEditText.getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                double price = Double.parseDouble(priceEditText.getText().toString());
                int quantity = Integer.parseInt(quantityEditText.getText().toString());

                // Create a new product document in Firestore
                Map<String, Object> product = new HashMap<>();
                product.put("productName", productName);
                product.put("category", category);
                product.put("price", price);
                product.put("quantity", quantity);

                productsCollection.add(product)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String productId = documentReference.getId();
                                // Upload the image chosen by the user
                                uploadImage(productId);
                                // Add success message or perform any other actions
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Add failure message or handle the error
                            }
                        });
            }
        });

        sellerInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open seller's profile activity
                Intent intent = new Intent(SellActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void uploadImage(String productId) {
        // Get the image file URI chosen by the user
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String productId = ""; // Get the product ID here
            StorageReference imageRef = storageRef.child(productId + ".jpg");

            // Upload the image file to Firebase Cloud Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image upload success
                            // Add success message or perform any other actions
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Image upload failed
                            // Add failure message or handle the error
                        }
                    });
        }
    }
}
