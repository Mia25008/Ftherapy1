package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftherapy.R;
import com.example.ftherapy.adapters.ProductAdapter;
import com.example.ftherapy.models.Product;
import com.example.ftherapy.models.User;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_catalog);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navUserName = headerView.findViewById(R.id.nav_user_name);
            TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
            TextView navUserInitial = headerView.findViewById(R.id.nav_user_initial);
            ImageView navUserImage = headerView.findViewById(R.id.nav_user_image);

            navUserName.setText(currentUser.getFullName());
            navUserEmail.setText(currentUser.getEmail());

            if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                navUserImage.setVisibility(View.VISIBLE);
                navUserInitial.setVisibility(View.GONE);

                try {
                    byte[] decodedString = android.util.Base64.decode(currentUser.getProfileImageUrl(), android.util.Base64.DEFAULT);
                    com.bumptech.glide.Glide.with(this)
                            .asBitmap()
                            .load(decodedString)
                            .circleCrop()
                            .into(navUserImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                navUserImage.setVisibility(View.GONE);
                navUserInitial.setVisibility(View.VISIBLE);

                if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                    navUserInitial.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
                }
            }

            User user = SharedPreferencesUtil.getUser(this);
            if (user != null && user.admin) {
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_ulist);
                if (adminItem != null) {
                    adminItem.setVisible(true);
                }
            }
        }


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_info) {
            startActivity(new Intent(this, InfoActivity.class));
            finish();
        } else if (id == R.id.nav_product) {
            // כבר פה
        } else if (id == R.id.nav_update) {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_custom, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnConfirm.setOnClickListener(v -> {
            SharedPreferencesUtil.signOutUser(this);
            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            alertDialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}