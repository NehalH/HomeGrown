package com.example.efarm;

import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.efarm.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private ListView productList;
    private ProductAdapter productAdapter;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        productList = findViewById(R.id.productList);

        products = new ArrayList<>();
        // Get the search query from the intent
        String searchQuery = getIntent().getStringExtra("searchQuery");

        // Call a method to populate the products based on the search query or category
        populateProductList(searchQuery);

        // Create and set the adapter for the product list
        productAdapter = new ProductAdapter(this, products);
        productList.setAdapter((ListAdapter) productAdapter);
    }

    private void populateProductList(String searchQuery) {
        // Here, you can implement your logic to fetch products based on the search query or category
        // For demonstration purposes, let's assume we have a hardcoded list of products

        // Clear the existing products list
        products.clear();

        // Simulate fetching products from a data source
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Display products based on the search query
            // You can replace this with your actual implementation
            List<Product> searchResults = searchProducts(searchQuery);
            products.addAll(searchResults);
        } else {
            // Display products based on a specific category
            // You can replace this with your actual implementation
            List<Product> categoryProducts = getProductsByCategory("Fruits");
            products.addAll(categoryProducts);
        }

        // Notify the adapter that the data set has changed
        productAdapter.notifyDataSetChanged();
    }

    private List<Product> searchProducts(String searchQuery) {
        // Implement your search logic here
        // This is a placeholder method that returns dummy products
        List<Product> searchResults = new ArrayList<>();
        // Replace this with your actual implementation
        // Search and filter products based on the search query
        // Add the matched products to the searchResults list
        // Example:
        searchResults.add(new Product("Apple", "Red Apple", 2.99));
        searchResults.add(new Product("Mango", "Sweet Mango", 3.49));
        return searchResults;
    }

    private List<Product> getProductsByCategory(String category) {
        // Implement your logic to fetch products by category here
        // This is a placeholder method that returns dummy products
        List<Product> categoryProducts = new ArrayList<>();
        // Replace this with your actual implementation
        // Fetch products based on the specified category
        // Add the fetched products to the categoryProducts list
        // Example:
        if (category.equalsIgnoreCase("Fruits")) {
            categoryProducts.add(new Product("Apple", "Red Apple", 2.99));
            categoryProducts.add(new Product("Mango", "Sweet Mango", 3.49));
            categoryProducts.add(new Product("Banana", "Yellow Banana", 1.99));
        } else if (category.equalsIgnoreCase("Vegetables")) {
            categoryProducts.add(new Product("Carrot", "Fresh Carrot", 1.49));
            categoryProducts.add(new Product("Tomato", "Ripe Tomato", 0.99));
            categoryProducts.add(new Product("Spinach", "Green Spinach", 1.99));
        }
        return categoryProducts;
    }
}
