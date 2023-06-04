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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        changeProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        listProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String productName = productNameEditText.getText().toString().trim();
                String category = getCategoryFromRadioButton();
                String quantityStr = quantityEditText.getText().toString().trim();
                String priceStr = priceEditText.getText().toString().trim();

                // Validate inputs
                if (productName.isEmpty()) {
                    Toast.makeText(SellActivity.this, "Please enter a product name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (category.isEmpty()) {
                    Toast.makeText(SellActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (quantityStr.isEmpty()) {
                    Toast.makeText(SellActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (priceStr.isEmpty()) {
                    Toast.makeText(SellActivity.this, "Please enter a price", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Parse quantity and price values
                double quantity = Double.parseDouble(quantityStr);
                double price = Double.parseDouble(priceStr);

                // Check if imageUri is null or not
                if (imageUri == null) {
                    Toast.makeText(SellActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new Product object
                Product product = new Product(productName, category, quantity, price, imageUri.toString());

                // Get current user UID
                String userId = mCurrentUser.getUid();

                // Get a reference to the "products" collection in Firestore
                firestore.collection("users").document(userId)
                        .collection("products").document()
                        .set(product)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Upload the image to Cloud Storage
                                uploadImage(userId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Listing failed
                                Toast.makeText(SellActivity.this, "Listing Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(String documentId) {
        if (imageUri != null) {
            // Get a reference to the product's image in Cloud Storage
            StorageReference imageRef = storageReference.child("products/" + documentId + "/product_image.jpg");

            // Upload the image file to Cloud Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image upload successful
                    Toast.makeText(SellActivity.this, "Product listed successfully!", Toast.LENGTH_SHORT).show();
                    // Reset fields after successful listing
                    resetFields();
                    openLoginActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Image upload failed
                    Toast.makeText(SellActivity.this, "Failed to upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No image selected, proceed with listing without uploading image
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
}
