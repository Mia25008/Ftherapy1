package com.example.ftherapy.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftherapy.R;
import com.example.ftherapy.adapters.UserAdapter;
import com.example.ftherapy.models.User;
import com.example.ftherapy.services.DatabaseService;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UsersListActivity";
    private DrawerLayout drawerLayout;
    private UserAdapter userAdapter;
    private TextView tvUserCount;
    private SearchView searchView;
    private List<User> fullUserList = new ArrayList<>();

    private DatabaseService databaseService = DatabaseService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupHeaderAndMenu(navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        RecyclerView usersList = findViewById(R.id.rv_users_list);
        tvUserCount = findViewById(R.id.tv_user_count);
        searchView = findViewById(R.id.search_view_users);

        usersList.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {
                showActionDialog(user);
            }
        });

        usersList.setAdapter(userAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void setupHeaderAndMenu(NavigationView navigationView) {
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
                    com.bumptech.glide.Glide.with(this).asBitmap().load(decodedString).circleCrop().into(navUserImage);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                navUserImage.setVisibility(View.GONE);
                navUserInitial.setVisibility(View.VISIBLE);
                if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                    navUserInitial.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
                }
            }

            MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_ulist);
            if (adminItem != null) adminItem.setVisible(true);
        }
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

        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            signOut();
            alertDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setDimAmount(0.7f);
        }

        alertDialog.show();
    }

    private void showActionDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_actions, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        Button btnAdmin = dialogView.findViewById(R.id.btn_dialog_admin);
        Button btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        tvTitle.setText("ניהול משתמש: " + user.getFirstName());
        btnAdmin.setText(user.admin ? "ביטול הרשאת מנהל" : "הפוך למנהל");

        btnAdmin.setOnClickListener(v -> {
            updateAdminStatus(user);
            alertDialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            confirmDeleteUser(user);
            alertDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    private void updateAdminStatus(User user) {
        boolean newStatus = !user.admin;
        databaseService.updateUserAdminStatus(user.getId(), newStatus, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UsersListActivity.this, "ההרשאות עודכנו", Toast.LENGTH_SHORT).show();
                onResume(); // רענון הרשימה
            }
            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersListActivity.this, "עדכון נכשל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("האם למחוק את " + user.getFullName() + "?")
                .setPositiveButton("מחק", (dialog, which) -> {
                    databaseService.deleteUser(user.getId(), new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(UsersListActivity.this, "משתמש נמחק", Toast.LENGTH_SHORT).show();
                            onResume();
                        }
                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(UsersListActivity.this, "מחיקה נכשלה", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void filter(String text) {
        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : fullUserList) {
            if (user.getFullName().toLowerCase().contains(text.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.setUserList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                fullUserList = new ArrayList<>(users);
                userAdapter.setUserList(users);
                tvUserCount.setText("סך כל המשתמשים: " + users.size());
            }
            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users", e);
            }
        });
    }

    private void signOut() {
        SharedPreferencesUtil.signOutUser(this);
        Intent intent = new Intent(this, LandingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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