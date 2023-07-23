package com.example.efarm;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirestoreAdapter adapter;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    private EditText searchEditText;
    private Button searchButton;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FirestoreAdapter();
        recyclerView.setAdapter(adapter);

        TextView profileTextView = findViewById(R.id.profileTextView);
        profileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                finish();
            }
        });

        TextView sellTextView = findViewById(R.id.sellTextView);
        sellTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SellActivity.class));
                finish();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString().trim();
                filterProducts(searchQuery);
            }
        });

        // Retrieve all products from Firestore initially
        retrieveDataFromFirestore();
    }

    private void retrieveDataFromFirestore() {
        firestore.collection("products").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QueryDocumentSnapshot> documentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documentList.add(document);
                        }
                        adapter.setData(documentList);
                    } else {
                        Toast.makeText(this, "An Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProducts(String searchQuery) {
        firestore.collection("products").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QueryDocumentSnapshot> documentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("productName");
                            if (productName != null && productName.toLowerCase().contains(searchQuery.toLowerCase())) {
                                documentList.add(document);
                            }
                        }
                        if (documentList.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                        }
                        adapter.setData(documentList);
                    } else {
                        Toast.makeText(this, "An Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (searchEditText.getText().toString().isEmpty()) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                finishAffinity();
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = System.currentTimeMillis();
        } else {
            searchEditText.setText("");
            retrieveDataFromFirestore();
        }
    }


    private class FirestoreAdapter extends RecyclerView.Adapter<FirestoreAdapter.ViewHolder> {

        private List<QueryDocumentSnapshot> data;

        public FirestoreAdapter() {
            data = new ArrayList<>();
        }

        public void setData(List<QueryDocumentSnapshot> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public QueryDocumentSnapshot getItem(int position) {
            return data.get(position);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item, parent, false);
            return new ViewHolder(view);
        }

        public String getPathFromContentUri(Uri contentUri) {
            String filePath = null;
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }

            return filePath;
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QueryDocumentSnapshot document = data.get(position);
            Product product = document.toObject(Product.class);

            holder.productName.setText(product.getProductName());
            holder.productCategory.setText(product.getCategory());
            holder.productQuantity.setText(String.valueOf(product.getQuantity()));
            holder.productPrice.setText(String.valueOf(product.getPrice()));

            // Load the product image
            String imagePath = "products/"+ product.getProductID() + "/product_image.jpg";
            System.out.println(imagePath);
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

            try {
                File localfile = File.createTempFile("temp_product_file", ".jpg");
                imageRef.getFile(localfile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                holder.productImage.setImageBitmap(bitmap);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this, "Faild to retreiv image", Toast.LENGTH_SHORT);

                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Set click listener for the item image
            holder.productImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the document ID of the clicked item
                    String productId = document.getId();

                    // Start the ProductDetailsActivity with the document ID
                    Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("productId", productId);
                    startActivity(intent);
                }
            });

            // Set click listener for the item details
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the document ID of the clicked item
                    String productId = document.getId();

                    // Start the ProductDetailsActivity with the document ID
                    Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                    intent.putExtra("productId", productId);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName;
            TextView productCategory;
            TextView productQuantity;
            TextView productPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productCategory = itemView.findViewById(R.id.productCategory);
                productQuantity = itemView.findViewById(R.id.productQuantity);
                productPrice = itemView.findViewById(R.id.productPrice);
            }
        }
    }
}
