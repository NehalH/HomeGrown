package com.example.efarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyProductActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private List<Product> productList;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private TextView emptyListTextView;
    private Button addProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_product);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        emptyListTextView = findViewById(R.id.emptyListTextView);
        addProductButton = findViewById(R.id.addProductButton);

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProductActivity.this, SellActivity.class);
                startActivity(intent);
            }
        });

        retrieveUserProducts();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyProductActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }


    private void retrieveUserProducts() {
        if (mCurrentUser != null) {
            final String userID = mCurrentUser.getUid();

            mFirestore.collection("products")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                productList.clear();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Get product data
                                    String productID = document.getString("productID");
                                    String productName = document.getString("productName");
                                    Long quantity = document.getLong("quantity");
                                    Long price = document.getLong("price");
                                    String category = document.getString("category");
                                    String imageURL = document.getString("imageURL");
                                    String userID = document.getString("userID");

                                    Product product = new Product(productName,category, quantity, price, imageURL, userID, productID );
                                    productList.add(product);
                                }

                                productAdapter.notifyDataSetChanged();

                                if (productList.isEmpty()) {
                                    showEmptyProductListUI();
                                } else {
                                    hideEmptyProductListUI();
                                }
                            } else {
                                Toast.makeText(MyProductActivity.this, "Failed to retrieve products: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void showEmptyProductListUI() {
        emptyListTextView.setVisibility(View.VISIBLE);
        addProductButton.setVisibility(View.VISIBLE);
    }

    private void hideEmptyProductListUI() {
        emptyListTextView.setVisibility(View.GONE);
        addProductButton.setVisibility(View.GONE);
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private List<Product> productList;

        public ProductAdapter(List<Product> productList) {
            this.productList = productList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = productList.get(position);

            holder.nameTextView.setText(product.getProductName());
            holder.categoryTextView.setText(product.getCategory());
            holder.priceTextView.setText("Price : "+ product.getPrice());
            holder.quantityTextView.setText("Quantity : "+ product.getQuantity());
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
                                holder.productImageView.setImageBitmap(bitmap);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MyProductActivity.this, "Failed to retrieve image", Toast.LENGTH_SHORT);

                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(productList.get(position).getProductID());
                }
            });
        }

        private void deleteProduct(final String productID) {
            if (productID != null && !productID.isEmpty()) {
                // Delete product document from "products" collection
                mFirestore.collection("products").document(productID)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Product deleted successfully
                                    Toast.makeText(MyProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                                    retrieveUserProducts(); // Refresh the product list
                                } else {
                                    // Failed to delete product
                                    Toast.makeText(MyProductActivity.this, "Failed to delete product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                // Handle the case when productID is null or empty
                Toast.makeText(MyProductActivity.this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            }
        }


        private void showDeleteConfirmationDialog(final String productID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyProductActivity.this);
            builder.setTitle("Delete Product");
            builder.setMessage("Are you sure you want to delete this product?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteProduct(productID);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView, priceTextView, quantityTextView, categoryTextView;
            private ImageView productImageView;
            private Button deleteButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                nameTextView = itemView.findViewById(R.id.nameTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                productImageView = itemView.findViewById(R.id.productImageView);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}


