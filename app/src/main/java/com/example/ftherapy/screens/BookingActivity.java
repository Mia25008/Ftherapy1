package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private LinearLayout layoutStep1, layoutStep2, layoutSuccess;
    private TextView circle1, circle2, circle3;
    private View line1, line2;
    private CalendarView calendarView;
    private GridLayout gridTimeSlots;
    private Button btnGoBack, btnConfirmTime, lastSelectedBtn = null;

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
            Calendar checkCal = Calendar.getInstance();
            checkCal.set(year, month, dayOfMonth);
            int dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) {
                Toast.makeText(this, "הקליניקה סגורה בשישי ושבת", Toast.LENGTH_LONG).show();
                selectedDate = "";
                gridTimeSlots.removeAllViews();
            } else {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                checkBusySlots(selectedDate);

                if (lastSelectedBtn != null) {
                    lastSelectedBtn.setSelected(false);
                    lastSelectedBtn = null;
                    selectedTime = "";
                }
            }
        });

        findViewById(R.id.btn_reiki).setOnClickListener(v -> { selectedTreatment = "רייקי וסאונד הילינג"; goToStep2(); });
        findViewById(R.id.btn_training).setOnClickListener(v -> { selectedTreatment = "אימון הוליסטי"; goToStep2(); });
        findViewById(R.id.btn_emr).setOnClickListener(v -> { selectedTreatment = "EMR"; goToStep2(); });

        btnConfirmTime.setOnClickListener(v -> saveAppointmentToFirebase());
        btnGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
        gridTimeSlots = findViewById(R.id.grid_time_slots);
        btnGoBack = findViewById(R.id.btn_go_back);
        btnConfirmTime = findViewById(R.id.btn_confirm_time);

        Calendar now = Calendar.getInstance();
        long minDate = System.currentTimeMillis();
        if (now.get(Calendar.HOUR_OF_DAY) >= 17) minDate += (24 * 60 * 60 * 1000);

        calendarView.setMinDate(minDate);
        calendarView.setMaxDate(minDate + (14L * 24 * 60 * 60 * 1000));

        Calendar minCal = Calendar.getInstance();
        minCal.setTimeInMillis(minDate);
        selectedDate = minCal.get(Calendar.DAY_OF_MONTH) + "/" + (minCal.get(Calendar.MONTH) + 1) + "/" + minCal.get(Calendar.YEAR);
    }

    private void checkBusySlots(String date) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Appointments");

        mDatabase.orderByChild("date").equalTo(date).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String[] allHours = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
                gridTimeSlots.removeAllViews();

                for (String hour : allHours) {
                    boolean isTaken = false;
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        Appointment app = snapshot.getValue(Appointment.class);
                        if (app != null && app.getTime().equals(hour)) {
                            isTaken = true;
                            break;
                        }
                    }

                    Button timeBtn = new Button(this);
                    timeBtn.setText(hour);
                    timeBtn.setAllCaps(false);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(10, 10, 10, 10);
                    timeBtn.setLayoutParams(params);

                    if (isTaken) {
                        timeBtn.setBackgroundResource(R.drawable.time_slot_busy_bg);
                        timeBtn.setEnabled(false);
                        timeBtn.setTextColor(Color.LTGRAY);
                    } else {
                        timeBtn.setBackgroundResource(R.drawable.time_slot_bg);
                        timeBtn.setEnabled(true);
                        timeBtn.setTextColor(Color.parseColor("#4A4A4A"));

                        timeBtn.setOnClickListener(v -> {
                            if (lastSelectedBtn != null) lastSelectedBtn.setSelected(false);
                            v.setSelected(true);
                            lastSelectedBtn = (Button) v;
                            selectedTime = hour;
                        });
                    }
                    gridTimeSlots.addView(timeBtn);
                }
            }
        });
    }


    private void saveAppointmentToFirebase() {
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "נא לבחור תאריך ושעה", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) return;

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Appointments");
        String id = mDatabase.push().getKey();
        Appointment newApp = new Appointment(id, currentUser.getId(), currentUser.getFullName(), selectedTreatment, selectedDate, selectedTime);

        if (id != null) {
            mDatabase.child(id).setValue(newApp).addOnSuccessListener(aVoid -> goToSuccess());
        }
    }

    private void setupNavHeader(NavigationView navigationView) {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null) {
            View headerView = navigationView.getHeaderView(0);
            ((TextView) headerView.findViewById(R.id.nav_user_name)).setText(currentUser.getFullName());
            ((TextView) headerView.findViewById(R.id.nav_user_email)).setText(currentUser.getEmail());
            ImageView img = headerView.findViewById(R.id.nav_user_image);
            TextView initial = headerView.findViewById(R.id.nav_user_initial);

            if (currentUser.isAdmin()) {
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_ulist);
                if (adminItem != null) {
                    adminItem.setVisible(true);
                }
            }

            if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                img.setVisibility(View.VISIBLE);
                initial.setVisibility(View.GONE);
                byte[] decoded = android.util.Base64.decode(currentUser.getProfileImageUrl(), android.util.Base64.DEFAULT);
                com.bumptech.glide.Glide.with(this).asBitmap().load(decoded).circleCrop().into(img);
            } else {
                img.setVisibility(View.GONE);
                initial.setVisibility(View.VISIBLE);
                if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                    initial.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
                }
            }
        }
    }
    private void goToStep2() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        circle1.setText("✓");
        circle1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        line1.setBackgroundColor(ContextCompat.getColor(this, R.color.step_done));
        circle2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_active)));

        checkBusySlots(selectedDate);
    }

    private void goToSuccess() {
        layoutStep2.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);
        circle2.setText("✓");
        circle2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        line2.setBackgroundColor(ContextCompat.getColor(this, R.color.step_done));
        circle3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.step_done)));
        circle3.setText("✓");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_main) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_info) {
            startActivity(new Intent(this, InfoActivity.class));
        } else if (id == R.id.nav_update) {
            startActivity(new Intent(this, UserProfileActivity.class));
        } else if (id == R.id.nav_product) {
            startActivity(new Intent(this, CatalogActivity.class));
        } else if (id == R.id.nav_booking) {
            startActivity(new Intent(this, BookingActivity.class));
        } else if (id == R.id.nav_ulist) {
            startActivity(new Intent(this, UsersListActivity.class));
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

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            SharedPreferencesUtil.signOutUser(this);
            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            alertDialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }
}