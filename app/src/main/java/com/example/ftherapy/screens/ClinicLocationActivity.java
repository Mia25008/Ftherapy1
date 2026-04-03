package com.example.ftherapy.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;

import com.example.ftherapy.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ClinicLocationActivity extends BaseActivity {

    private MapView map = null;

    private final double CLINIC_LAT = 31.96798413952502;
    private final double CLINIC_LON = 34.77902680630169;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת ספריית המפות - חשוב מאוד להריץ לפני setContentView
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_clinic_location);

        map = findViewById(R.id.map_view);
        map.setMultiTouchControls(true); // מאפשר זום עם האצבעות

        // יצירת נקודה גיאוגרפית
        GeoPoint clinicPoint = new GeoPoint(CLINIC_LAT, CLINIC_LON);

        map.getController().setZoom(18.0);
        map.getController().setCenter(clinicPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(clinicPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("הקליניקה של אפרת");
        startMarker.setSnippet("אנחנו מחכים לכם כאן");
        map.getOverlays().add(startMarker);

        // כפתור ניווט לגוגל מפות (פותח את האפליקציה החיצונית)
        Button btnNavigate = findViewById(R.id.btn_navigate);
        btnNavigate.setOnClickListener(v -> {
            Uri navigationUri = Uri.parse("google.navigation:q=" + CLINIC_LAT + "," + CLINIC_LON);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // בדיקה אם מותקנת אפליקציית מפות, ואם לא - פתיחה בדפדפן
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + CLINIC_LAT + "," + CLINIC_LON);
                startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}