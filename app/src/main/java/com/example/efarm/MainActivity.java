package com.example.efarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button searchButton;
    private EditText searchEditText;
    private TextView profileTextView, sellTextView, homeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        searchButton = findViewById(R.id.searchButton);
        searchEditText = findViewById(R.id.searchEditText);
        profileTextView = findViewById(R.id.profileTextView);
        sellTextView = findViewById(R.id.sellTextView);
        homeTextView = findViewById(R.id.homeTextView);

        // Set click listener for search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform search based on the entered text
                String searchText = searchEditText.getText().toString().trim();
                // Implement your search logic here
                Toast.makeText(MainActivity.this, "Searching for: " + searchText, Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for profile
        profileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ProfileActivity
                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        // Set click listener for sell
        sellTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open SellActivity
                Intent sellIntent = new Intent(MainActivity.this, SellActivity.class);
                startActivity(sellIntent);
            }
        });

        // Set click listener for home
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
