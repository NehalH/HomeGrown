package com.example.efarm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.efarm.MainActivity;
import com.example.efarm.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SellActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView productImageView;
    private Button changeProductImageButton;
    private EditText productNameEditText;
    private RadioGroup categoryRadioGroup;
    private EditText quantityEditText;
    private EditText priceEditText;
    private Button listProductButton;
    private ProgressBar progressBar;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        productImageView = findViewById(R.id.productImageView);
        changeProductImageButton = findViewById(R.id.changeProductImageButton);
        productNameEditText = findViewById(R.id.productNameEditText);
        categoryRadioGroup = findViewById(R.id.categoryRadioGroup);
        quantityEditText = findViewById(R.id.quantityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        listProductButton = findViewById(R.id.listProductButton);
        progressBar = findViewById(R.id.progressBar);

        changeProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        listProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    // Get user input
                    String productName = productNameEditText.getText().toString().trim();
                    String category = getCategoryFromRadioButton();
                    double quantity = Double.parseDouble(quantityEditText.getText().toString().trim());
                    double price = Double.parseDouble(priceEditText.getText().toString().trim());

                    // Show progress indicator
                    showProgressBar();

                    // Check if imageUri is null or not
                    String imageUrl = imageUri != null ? imageUri.toString() : ""; // Set imageUrl to empty string if imageUri is null

                    // Create a new Product object
                    Product product = new Product(productName, category, quantity, price, imageUrl);

                    // Get current user UID
                    String userId = mCurrentUser.getUid();

                    // Get a reference to the "products" collection in Firestore
                    DocumentReference productRef = firestore.collection("users").document(userId)
                            .collection("products").document();

                    // Set the product data in Firestore
                    productRef.set(product)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Upload the image to Cloud Storage
                                    uploadImage(userId, productRef.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Listing failed
                                    hideProgressBar();
                                    Toast.makeText(SellActivity.this, "Listing Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(SellActivity.this, "Please enter valid data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(String userId, String productId) {
        if (imageUri != null) {
            // Get a reference to the product's image in Cloud Storage
            StorageReference imageRef = storageReference.child("products/" + userId + "/" + productId + "/product_image.jpg");

            // Upload the image file to Cloud Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image upload successful
                    hideProgressBar();
                    Toast.makeText(SellActivity.this, "Product listed successfully!", Toast.LENGTH_SHORT).show();
                    // Reset fields after successful listing
                    resetFields();
                    openLoginActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Image upload failed
                    hideProgressBar();
                    Toast.makeText(SellActivity.this, "Failed to upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No image selected, proceed with listing without uploading image
            hideProgressBar();
            Toast.makeText(SellActivity.this, "No image selected. Listing Successful!", Toast.LENGTH_SHORT).show();
            // Reset fields after successful listing
            resetFields();
            openLoginActivity();
        }
    }

    private void resetFields() {
        productImageView.setImageResource(R.drawable.baseline_person_24);
        productNameEditText.setText("");
        categoryRadioGroup.check(R.id.vegetableRadioButton);
        quantityEditText.setText("");
        priceEditText.setText("");
    }

    private String getCategoryFromRadioButton() {
        int selectedRadioButtonId = categoryRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        return selectedRadioButton.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            productImageView.setImageURI(imageUri);
        }
    }

    private void openLoginActivity() {
        Intent intent = new Intent(SellActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateFields() {
        boolean isValid = true;

        String productName = productNameEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();

        if (productName.isEmpty()) {
            productNameEditText.setError("Product name is required");
            isValid = false;
        }

        if (quantity.isEmpty()) {
            quantityEditText.setError("Quantity is required");
            isValid = false;
        }

        if (price.isEmpty()) {
            priceEditText.setError("Price is required");
            isValid = false;
        }

        try {
            Double.parseDouble(quantity);
        } catch (NumberFormatException e) {
            isValid = false;
        }

        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            isValid = false;
        }

        return isValid;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
