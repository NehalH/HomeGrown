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
import android.widget.TextView;
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
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
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
                    String productName = productNameEditText.getText().toString().trim();
                    String category = getCategoryFromRadioButton();
                    double quantity = Double.parseDouble(quantityEditText.getText().toString().trim());
                    double price = Double.parseDouble(priceEditText.getText().toString().trim());
                    showProgressBar();
                    String imageUrl = imageUri != null ? imageUri.toString() : "";
                    String userId = mCurrentUser.getUid();
                    String productId = firestore.collection("products").document().getId();
                    Product product = new Product(productName, category, quantity, price, imageUrl, userId, productId);
                    DocumentReference productRef = firestore.collection("products").document(productId);
                    productRef.set(product)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    uploadImage(productId);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    hideProgressBar();
                                    Toast.makeText(SellActivity.this, "Listing Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(SellActivity.this, "Please enter valid data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView profileTextView = findViewById(R.id.profileTextView);
        profileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellActivity.this, ProfileActivity.class));
                finish();
            }
        });

        TextView homeTextView = findViewById(R.id.homeTextView);
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SellActivity.this, MainActivity.class));
                finish();
            }
        });
    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(String productId) {
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("products/" + productId + "/product_image.jpg");

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    hideProgressBar();
                    Toast.makeText(SellActivity.this, "Product listed successfully!", Toast.LENGTH_SHORT).show();
                    resetFields();
                    openLoginActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    Toast.makeText(SellActivity.this, "Failed to upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            hideProgressBar();
            Toast.makeText(SellActivity.this, "No image selected. Listing Successful!", Toast.LENGTH_SHORT).show();
            resetFields();
            openLoginActivity();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SellActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
