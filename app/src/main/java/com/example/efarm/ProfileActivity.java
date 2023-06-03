package com.example.efarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorageRef;
    private FirebaseUser mCurrentUser;
    private TextView nameTextView, phoneTextView, emailTextView, addressTextView;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Get current user
        mCurrentUser = mAuth.getCurrentUser();

        // Initialize views
        nameTextView = findViewById(R.id.nametextView);
        phoneTextView = findViewById(R.id.phonetextView);
        emailTextView = findViewById(R.id.emailtextView);
        addressTextView = findViewById(R.id.addresstextView);
        profileImageView = findViewById(R.id.imageView);

        // Retrieve user data from Firestore
        retrieveUserData();

        // Set click listeners for navigation
        TextView homeTextView = findViewById(R.id.homeTextView);
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        });

        TextView sellTextView = findViewById(R.id.sellTextView);
        sellTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SellActivity.class));
                finish();
            }
        });
    }

    private void retrieveUserData() {
        if (mCurrentUser != null) {
            String userId = mCurrentUser.getUid();

            // Retrieve user data from Firestore
            mFirestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Get user data from the document snapshot
                                String name = documentSnapshot.getString("name");
                                String phone = documentSnapshot.getString("phone");
                                String email = documentSnapshot.getString("email");
                                String address = documentSnapshot.getString("address");
                                String imageUrl = documentSnapshot.getString("imageUrl");

                                // Set user data to the corresponding views
                                nameTextView.setText(name);
                                phoneTextView.setText(phone);
                                emailTextView.setText(email);
                                addressTextView.setText(address);

                                // Load profile image using Glide library
                                RequestOptions requestOptions = new RequestOptions()
                                        .placeholder(R.drawable.default_profile_image_background)
                                        .error(R.drawable.default_profile_image_foreground);

                                Glide.with(ProfileActivity.this)
                                        .load(imageUrl)
                                        .apply(requestOptions)
                                        .into(profileImageView);
                            } else {
                                // Document does not exist
                                Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the error
                            Toast.makeText(ProfileActivity.this, "Failed to retrieve user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
