package com.example.ftherapy.screens;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ftherapy.R;

public class ProductDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView imageView = findViewById(R.id.detailImage);
        TextView textView = findViewById(R.id.detailName);

        String name = getIntent().getStringExtra("product_name");
        int image = getIntent().getIntExtra("product_image", 0);

        textView.setText(name);
        imageView.setImageResource(image);
    }
}
