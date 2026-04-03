package com.example.ftherapy.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ftherapy.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class InfoActivity extends AppCompatActivity {

    private MapView map = null;

    private final double CLINIC_LAT = 31.96798413952502;
    private final double CLINIC_LON = 34.77902680630169;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        setupToolbar();
        setupMap();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //אתחול המפה והכפתורים
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

            //אם אין אפליקציית מפות, פתיחה בדפדפן
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + CLINIC_LAT + "," + CLINIC_LON)));
            }
        });
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

    // ניהול מחזור החיים של המפה על מנת למנוע קריסות
    @Override
    public void onResume() { super.onResume(); if(map != null) map.onResume(); }
    @Override
    public void onPause() { super.onPause(); if(map != null) map.onPause(); }
}