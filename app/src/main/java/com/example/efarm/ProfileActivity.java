package com.example.efarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        nameTextView = findViewById(R.id.nametextView);
        phoneTextView = findViewById(R.id.phonetextView);
        emailTextView = findViewById(R.id.emailtextView);
        addressTextView = findViewById(R.id.addresstextView);
        profileImageView = findViewById(R.id.imageView);

        retrieveUserData();

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

        Button editDetailsButton = findViewById(R.id.editDetailsButton);
        editDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditDetailsActivity.class));
                finish();
            }
        });

        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });
        Button myProductsButton = findViewById(R.id.myProductsButton);
        myProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MyProductActivity.class));
            }
        });
    }

    private void retrieveUserData() {
        if (mCurrentUser != null) {
            String userID = mCurrentUser.getUid();
            mFirestore.collection("users").document(userID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                String phone = documentSnapshot.getString("phone");
                                String email = documentSnapshot.getString("email");
                                String address = documentSnapshot.getString("address");

                                String bucketName = "gs://homegrown-775ae.appspot.com";
                                String imagePath = "users/"+ userID + "/profile_image.jpg";

                                nameTextView.setText(name);
                                phoneTextView.setText(phone);
                                emailTextView.setText(email);
                                addressTextView.setText(address);

                                mStorageRef = FirebaseStorage.getInstance().getReference(imagePath);

                                try {
                                    File localfile = File.createTempFile("tempfile", ".jpg");
                                    mStorageRef.getFile(localfile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                    profileImageView.setImageBitmap(bitmap);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(ProfileActivity.this, "Faild to retreiv image", Toast.LENGTH_SHORT);

                                                }
                                            });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
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
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteAccount() {
        if (mCurrentUser != null) {
            String userID = mCurrentUser.getUid();

            WriteBatch batch = mFirestore.batch();

            batch.delete(mFirestore.collection("users").document(userID));

            mFirestore.collection("products")
                    .whereEqualTo("userId", userID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            List<QueryDocumentSnapshot> productsToDelete = new ArrayList<>();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                productsToDelete.add(document);
                            }

                            for (QueryDocumentSnapshot productDocument : productsToDelete) {
                                batch.delete(productDocument.getReference());
                            }

                            batch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            String bucketName = "gs://homegrown-775ae.appspot.com";
                                            String imagePath = "users/" + userID + "/profile_image.jpg";
                                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bucketName + "/" + imagePath);
                                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(ProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ProfileActivity.this, "Failed to delete profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProfileActivity.this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

