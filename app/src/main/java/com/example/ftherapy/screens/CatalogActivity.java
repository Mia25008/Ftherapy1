package com.example.ftherapy.screens;

import android.view.MenuItem;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
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

        setupToolbar();

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        List<Product> list = new ArrayList<>();
        // כאן את מוסיפה מוצרים עם תמונות מתיקיית ה-drawable שלך
        list.add(new Product("שמן לבנדר", R.drawable.lavend));
        list.add(new Product("מבער", R.drawable.burner));
        list.add(new Product("קרם פנים", R.drawable.cream));

        rv.setAdapter(new ProductAdapter(list));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // סגירת המסך וחזרה אחורה
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                // שתי השורות האלו יוצרות את החץ
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }
}