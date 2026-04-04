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

        list.add(new Product("שמן לבנדר", R.drawable.lavend, "שמן אתרי טהור להרגעה עמוקה, סיוע בשינה איכותית והפגת מתחים."));
        list.add(new Product("מבער", R.drawable.burner, "מבער קרמי מעוצב להפצת ניחוחות משכרים ויצירת אווירה קסומה בחלל."));
        list.add(new Product("קרם פנים", R.drawable.cream, "פורמולה עשירה בוויטמינים להזנה עמוקה, מיצוק והענקת מראה זוהר לעור."));
        list.add(new Product("קרם גוף", R.drawable.cream, "מרקם קטיפתי בניחוח עדין הנספג במהירות ומעניק לחות לאורך כל היום."));
        list.add(new Product("שפתון לחות", R.drawable.cream, "טיפול אינטנסיבי לשפתיים יבשות, מבוסס על חמאת שיאה ושמנים טבעיים."));
        list.add(new Product("שמן גוף", R.drawable.cream, "תערובת שמנים יוקרתית לעיסוי והזנה, משאירה את העור רך ומבוסם."));
        list.add(new Product("בושם שמן", R.drawable.cream, "ניחוח מרוכז ועמיד לאורך זמן על בסיס שמן, ללא אלכוהול, עדין לעור."));
        list.add(new Product("מבשם אוויר", R.drawable.cream, "תרסיס מרענן המנטרל ריחות ומשרה תחושת ניקיון ורוגע בכל חדר."));

        rv.setAdapter(new ProductAdapter(list));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }
}