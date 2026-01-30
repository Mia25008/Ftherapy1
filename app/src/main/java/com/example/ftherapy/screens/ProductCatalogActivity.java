package com.example.ftherapy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
        list.add(new Product("טיפול פנים", R.drawable.face_care));
        list.add(new Product("עיסוי אבנים חמות", R.drawable.massage));
        list.add(new Product("רפלקסולוגיה", R.drawable.foot_care));

        rv.setAdapter(new ProductAdapter(list));
    }
}