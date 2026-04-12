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

import com.example.ftherapy.R;
import com.example.ftherapy.models.User;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private String selectedUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_user_name);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
        TextView navUserInitial = headerView.findViewById(R.id.nav_user_initial);
        ImageView navUserImage = headerView.findViewById(R.id.nav_user_image);

        navUserName.setText("שלום, " + currentUser.getFullName());
        navUserEmail.setText(currentUser.getEmail());

        String imageBase64 = currentUser.getProfileImageUrl();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            navUserImage.setVisibility(View.VISIBLE);
            navUserInitial.setVisibility(View.GONE);

            try {
                byte[] decodedString = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                com.bumptech.glide.Glide.with(this)
                        .asBitmap()
                        .load(decodedString)
                        .circleCrop()
                        .into(navUserImage);
            } catch (Exception e) {
                navUserImage.setVisibility(View.GONE);
                navUserInitial.setVisibility(View.VISIBLE);
                setInitial(navUserInitial, currentUser.getFullName());
            }
        } else {
            navUserImage.setVisibility(View.GONE);
            navUserInitial.setVisibility(View.VISIBLE);
            setInitial(navUserInitial, currentUser.getFullName());
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (currentUser.isAdmin()) {
            navigationView.getMenu().findItem(R.id.nav_ulist).setVisible(true);
        }

        selectedUserid = getIntent().getStringExtra("USER_UID");
        if (selectedUserid == null) {
            selectedUserid = currentUser.getId();
        }
    }

    private void setInitial(TextView textView, String fullName) {
        if (fullName != null && !fullName.isEmpty()) {
            textView.setText(fullName.substring(0, 1).toUpperCase());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_info) {
            startActivity(new Intent(MainActivity.this, InfoActivity.class));
        } else if (id == R.id.nav_update) {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        } else if (id == R.id.nav_product) {
            startActivity(new Intent(MainActivity.this, CatalogActivity.class));
        } else if (id == R.id.nav_booking) {
            startActivity(new Intent(MainActivity.this, BookingActivity.class));
        } else if (id == R.id.nav_ulist) {
            startActivity(new Intent(MainActivity.this, UsersListActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_custom, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            SharedPreferencesUtil.signOutUser(MainActivity.this);
            Intent intent = new Intent(MainActivity.this, LandingActivity.class);
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