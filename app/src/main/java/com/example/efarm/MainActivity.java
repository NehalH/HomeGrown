package com.example.efarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirestoreAdapter adapter;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        // Set layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Create and set the adapter
        adapter = new FirestoreAdapter();
        recyclerView.setAdapter(adapter);

        // Retrieve data from Firestore and populate the adapter
        retrieveDataFromFirestore();

        // Set click listeners for navigation
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
    }

    private void retrieveDataFromFirestore() {
        firestore.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // List to hold the retrieved documents
                        List<QueryDocumentSnapshot> documentList = new ArrayList<>();

                        // Iterate through the documents and add them to the list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documentList.add(document);
                        }

                        // Retrieve data from secondary collections for each document
                        retrieveDataFromSecondaryCollections(documentList);
                    } else {
                        // Failed to retrieve documents
                        // Handle the error
                    }
                });
    }

    private void retrieveDataFromSecondaryCollections(List<QueryDocumentSnapshot> documentList) {
        // Iterate through the documents
        for (QueryDocumentSnapshot document : documentList) {
            // Retrieve the reference to the secondary collection for the current document
            CollectionReference secondaryCollectionRef = document.getReference().collection("products");

            // Retrieve documents from the secondary collection
            secondaryCollectionRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // List to hold the retrieved secondary documents
                            List<QueryDocumentSnapshot> secondaryDocumentList = new ArrayList<>();

                            // Iterate through the secondary documents and add them to the list
                            for (QueryDocumentSnapshot secondaryDocument : task.getResult()) {
                                secondaryDocumentList.add(secondaryDocument);
                            }

                            // Update the data in the adapter
                            adapter.setData(secondaryDocumentList);
                        } else {
                            // Failed to retrieve secondary documents
                            // Handle the error
                        }
                    });
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            QueryDocumentSnapshot document = data.get(position);
            Product product = document.toObject(Product.class);

            // Customize the view holder content based on the Product object
            holder.productName.setText(product.getProductName());
            holder.productCategory.setText(product.getCategory());
            holder.productQuantity.setText("Available : " + String.valueOf(product.getQuantity()) + " kg");
            holder.productPrice.setText("Price : ₹ " + String.valueOf(product.getPrice()) + " /kg");

            // Load the product image using Picasso
            Picasso.get()
                    .load(product.getImageUrl())
                    .into(holder.productImage);

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
