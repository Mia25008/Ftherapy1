package com.example.ftherapy.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftherapy.R;
import com.example.ftherapy.adapters.ProductAdapter;
import com.example.ftherapy.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_catalog);

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        List<Product> list = new ArrayList<>();
        // כאן את מוסיפה מוצרים עם תמונות מתיקיית ה-drawable שלך
        list.add(new Product("שמן לבנדר", R.drawable.lavend));
        list.add(new Product("מבער", R.drawable.burner));
        list.add(new Product("קרם פנים", R.drawable.cream));

        rv.setAdapter(new ProductAdapter(list));
    }
}