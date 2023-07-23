package com.example.efarm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class BookProductActivity extends AppCompatActivity {
    private TextView sellerNameTextView;
    private TextView sellerPhoneTextView;
    private TextView sellerEmailTextView;
    private MaterialButton callButton;
    private MaterialButton emailButton;
    private MaterialButton waButton;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_product);
        sellerNameTextView = findViewById(R.id.sellerNameTextView);
        sellerNameTextView = findViewById(R.id.sellerNameTextView);
        callButton = findViewById(R.id.callButton);
        emailButton = findViewById(R.id.emailButton);
        waButton = findViewById(R.id.wa);

        String productId = getIntent().getStringExtra("productId");

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("products").document(productId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String userId = document.getString("userID");
                                firestore.collection("users").document(userId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> sellerTask) {
                                                if (sellerTask.isSuccessful()) {
                                                    DocumentSnapshot sellerDocument = sellerTask.getResult();
                                                    if (sellerDocument != null && sellerDocument.exists()) {
                                                        String sellerName = sellerDocument.getString("name");
                                                        String sellerPhone = sellerDocument.getString("phone");
                                                        String sellerEmail = sellerDocument.getString("email");

                                                        sellerNameTextView.setText(sellerName);
                                                        callButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialPhoneNumber(sellerPhone);
                                                            }
                                                        });

                                                        emailButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                sendEmail(sellerEmail);
                                                            }
                                                        });

                                                        waButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                whatsapp(sellerPhone);
                                                            }
                                                        });

                                                    } else {
                                                        // Seller document does not exist
                                                    }
                                                } else {
                                                    Exception exception = sellerTask.getException();
                                                    if (exception != null) {

                                                    }
                                                }
                                            }
                                        });
                            } else {
                                // Product document does not exist
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseFirestoreException) {
                                // Handle the exception
                            }
                        }
                    }
                });
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unable to perform call action", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String emailAddress) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + emailAddress));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unable to perform email action", Toast.LENGTH_SHORT).show();
        }
    }
    private void whatsapp(String phoneNumber) {
        try {
            String formattedPhoneNumber = "+91" + phoneNumber;
            String message = "Hello, I'm interested in your product.";
            String apiURL = "https://api.whatsapp.com/send?phone=" + formattedPhoneNumber + "&text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apiURL));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to perform WhatsApp action", Toast.LENGTH_SHORT).show();
        }
    }

}
