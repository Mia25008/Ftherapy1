package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ftherapy.R;
import com.example.ftherapy.models.Appointment;
import com.example.ftherapy.models.User;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private LinearLayout layoutStep1, layoutStep2, layoutSuccess;
    private TextView circle1, circle2, circle3, tvSelectedTime;
    private View line1, line2;
    private CalendarView calendarView;

    private String selectedTreatment = "";
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupNavHeader(navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        initViews();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            showTimePicker();
        });

        findViewById(R.id.btn_reiki).setOnClickListener(v -> {
            selectedTreatment = "רייקי וסאונד הילינג";
            goToStep2();
        });
        findViewById(R.id.btn_training).setOnClickListener(v -> {
            selectedTreatment = "אימון הוליסטי";
            goToStep2();
        });
        findViewById(R.id.btn_emr).setOnClickListener(v -> {
            selectedTreatment = "EMR";
            goToStep2();
        });

        // כפתור אישור סופי
        findViewById(R.id.btn_confirm_time).setOnClickListener(v -> saveAppointmentToFirebase());
    }

    private void setupNavHeader(NavigationView navigationView) {
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

            if (currentUser.admin) {
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_ulist);
                if (adminItem != null) {
                    adminItem.setVisible(true);
                }
            }
        }
    }

    private void initViews() {
        layoutStep1 = findViewById(R.id.layout_step1);
        layoutStep2 = findViewById(R.id.layout_step2);
        layoutSuccess = findViewById(R.id.layout_success);

        circle1 = findViewById(R.id.step1_circle);
        circle2 = findViewById(R.id.step2_circle);
        circle3 = findViewById(R.id.step3_circle);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
    }

    private void showTimePicker() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(BookingActivity.this, (timePicker, selectedHour, selectedMinute) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            tvSelectedTime.setText("השעה שנבחרה: " + selectedTime);
            Toast.makeText(BookingActivity.this, "השעה נשמרה", Toast.LENGTH_SHORT).show();
        }, hour, minute, true);

        mTimePicker.setTitle("בחר שעה לתור");
        mTimePicker.show();
    }

    private void saveAppointmentToFirebase() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "בבקשה בחר תאריך בלוח השנה", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "בבקשה בחר שעה", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) return;

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Appointments");
        String appointmentId = mDatabase.push().getKey();

        Appointment newApp = new Appointment(
                appointmentId,
                currentUser.getId(),
                currentUser.getFullName(),
                selectedTreatment,
                selectedDate,
                selectedTime
        );

        if (appointmentId != null) {
            mDatabase.child(appointmentId).setValue(newApp)
                    .addOnSuccessListener(aVoid -> goToSuccess())
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void goToStep2() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        circle1.setText("✓");
        circle1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        line1.setBackgroundColor(ContextCompat.getColor(this, R.color.step_done));
        circle2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_active)));
        circle2.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    private void goToSuccess() {
        layoutStep2.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);
        circle2.setText("✓");
        circle2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        circle2.setTextColor(ContextCompat.getColor(this, R.color.white));
        line2.setBackgroundColor(ContextCompat.getColor(this, R.color.step_done));
        circle3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        circle3.setText("✓");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_info) {
            startActivity(new Intent(this, InfoActivity.class));
            finish();
        } else if (id == R.id.nav_update) {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        } else if (id == R.id.nav_product) {
            startActivity(new Intent(this, CatalogActivity.class));
            finish();
        } else if (id == R.id.nav_ulist) {
            startActivity(new Intent(this, UsersListActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_custom, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(BookingActivity.this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            SharedPreferencesUtil.signOutUser(BookingActivity.this);
            Intent intent = new Intent(BookingActivity.this, LandingActivity.class);
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