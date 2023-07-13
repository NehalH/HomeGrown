package com.example.efarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ProductDetailsActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView productNameTextView;
    private TextView categoryTextView;
    private TextView quantityTextView;
    private TextView priceTextView;
    private TextView sellerTextView;
    private TextView phoneTextView;
    private TextView addressTextView;
    private Button bookButton;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Find the views by their IDs
        imageView = findViewById(R.id.imageView);
        productNameTextView = findViewById(R.id.productnametextView);
        categoryTextView = findViewById(R.id.categorytextView);
        quantityTextView = findViewById(R.id.quantitytextView);
        priceTextView = findViewById(R.id.pricetextView);
        sellerTextView = findViewById(R.id.sellertextView);
        phoneTextView = findViewById(R.id.phonetextView);
        addressTextView = findViewById(R.id.addresstextView);
        bookButton = findViewById(R.id.bookButton);

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
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Get the product details
                                String productName = document.getString("productName");
                                String category = document.getString("category");
                                Long quantity = document.getLong("quantity");
                                String quantityString = String.valueOf(quantity);
                                Long price = document.getLong("price");
                                String priceString = String.valueOf(price); // Convert to String
                                String userId = document.getString("userID");
                                String imageUrl = document.getString("imageUrl");
                                String productID = document.getString("productID");

                                // Set the product details to the respective views
                                productNameTextView.setText(productName);
                                categoryTextView.setText(category);
                                quantityTextView.setText(quantityString+"  kg");
                                priceTextView.setText(priceString+"  per kg");

                                // Retrieve the seller details from Firestore
                                firestore.collection("users").document(userId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(Task<DocumentSnapshot> sellerTask) {
                                                if (sellerTask.isSuccessful()) {
                                                    DocumentSnapshot sellerDocument = sellerTask.getResult();
                                                    if (sellerDocument != null && sellerDocument.exists()) {
                                                        String sellerName = sellerDocument.getString("name");
                                                        String sellerPhone = sellerDocument.getString("phone");
                                                        String sellerAddress = sellerDocument.getString("address");

                                                        // Set the seller details to the respective views
                                                        sellerTextView.setText(sellerName);
                                                        phoneTextView.setText(sellerPhone);
                                                        addressTextView.setText(sellerAddress);
                                                    } else {
                                                        // Seller document does not exist
                                                    }
                                                } else {
                                                    // Error fetching seller document
                                                    Exception exception = sellerTask.getException();
                                                    if (exception != null) {
                                                        // Handle the exception
                                                    }
                                                }
                                            }
                                        });

                                // Load the image using Glide
                                String imagePath = "products/"+ productID + "/product_image.jpg";
                                System.out.println(imagePath);
                                StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

                                try {
                                    File localfile = File.createTempFile("temp_product_file", ".jpg");
                                    imageRef.getFile(localfile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                    imageView.setImageBitmap(bitmap);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(ProductDetailsActivity.this, "Failed to retrieve image", Toast.LENGTH_SHORT);

                                                }
                                            });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                // Product document does not exist
                            }
                        } else {
                            // Error fetching product document
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseFirestoreException) {
                                // Handle the exception
                            }
                        }
                    }
                });

        // Set a click listener to the "Book" button
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click
                bookProduct(productId);
            }
        });
    }

    private void bookProduct(String productId) {
        // Create an intent to start the BookProductActivity
        Intent intent = new Intent(ProductDetailsActivity.this, BookProductActivity.class);
        // Pass the product ID to the BookProductActivity
        intent.putExtra("productId", productId);
        // Start the BookProductActivity
        startActivity(intent);
    }
}
