package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ftherapy.R;
import com.example.ftherapy.models.User;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class InfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MapView map = null;
    private DrawerLayout drawerLayout;
    private final double CLINIC_LAT = 31.96798413952502;
    private final double CLINIC_LON = 34.77902680630169;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

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

        setupMap();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupMap() {
        map = findViewById(R.id.map_view);
        map.setMultiTouchControls(true);
        GeoPoint clinicPoint = new GeoPoint(CLINIC_LAT, CLINIC_LON);
        map.getController().setZoom(18.0);
        map.getController().setCenter(clinicPoint);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(clinicPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("הקליניקה של אפרת");
        map.getOverlays().add(startMarker);

        Button btnNavigate = findViewById(R.id.btn_navigate);
        btnNavigate.setOnClickListener(v -> {
            Uri navigationUri = Uri.parse("google.navigation:q=" + CLINIC_LAT + "," + CLINIC_LON);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + CLINIC_LAT + "," + CLINIC_LON)));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_info) {
            // כבר פה
        } else if (id == R.id.nav_product) {
            startActivity(new Intent(this, CatalogActivity.class));
            finish();
        } else if (id == R.id.nav_update) {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        } else if (id == R.id.nav_booking) {
            startActivity(new Intent(this, BookingActivity.class));
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
    public void onResume() { super.onResume(); if(map != null) map.onResume(); }
    @Override
    public void onPause() { super.onPause(); if(map != null) map.onPause(); }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}