package com.example.ftherapy.screens;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ftherapy.R;

public class ProductDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView imageView = findViewById(R.id.detailImage);
        TextView nameTextView = findViewById(R.id.detailName);
        TextView descTextView = findViewById(R.id.detailDescription);

        String name = getIntent().getStringExtra("product_name");
        String description = getIntent().getStringExtra("product_description");
        int imageResId = getIntent().getIntExtra("product_image", 0);

        if (name != null) {
            nameTextView.setText(name);
        }

        if (description != null) {
            descTextView.setText(description);
        }

        if (imageResId != 0) {
            imageView.setImageResource(imageResId);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}