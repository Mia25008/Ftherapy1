package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.ftherapy.R;
import com.example.ftherapy.models.User;
import com.example.ftherapy.services.DatabaseService;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.example.ftherapy.utils.Validator;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserProfileActivity";
    private DrawerLayout drawerLayout;
    private EditText etUserFirstName, etUserLastName, etUserEmail, etUserPhone, etUserPassword;
    private TextView tvUserDisplayName, tvUserDisplayEmail;
    private Button btnUpdateProfile, btnSignOut;
    private View adminBadge;
    private ImageView ivProfilePicture;
    private RelativeLayout rlImageContainer;

    private String selectedUid;
    private User selectedUser;
    private boolean isCurrentUser = false;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> galleryLauncher;

    private DatabaseService databaseService = DatabaseService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        initViews();
        setupNavigation();

        selectedUid = getIntent().getStringExtra("USER_UID");
        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser == null) {
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        if (selectedUid == null) {
            selectedUid = currentUser.getId();
        }

        isCurrentUser = selectedUid.equals(currentUser.getId());

        if (!currentUser.admin && !isCurrentUser) {
            Toast.makeText(this, "אינך מורשה לצפות בפרופיל זה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfilePicture.setImageURI(uri);
                    }
                }
        );

        rlImageContainer.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnUpdateProfile.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);

        if (!isCurrentUser) {
            btnSignOut.setVisibility(View.GONE);
        }

        showUserProfile();
    }

    private void initViews() {
        etUserFirstName = findViewById(R.id.et_user_first_name);
        etUserLastName = findViewById(R.id.et_user_last_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserPhone = findViewById(R.id.et_user_phone);
        etUserPassword = findViewById(R.id.et_user_password);
        tvUserDisplayName = findViewById(R.id.tv_user_display_name);
        tvUserDisplayEmail = findViewById(R.id.tv_user_display_email);
        btnUpdateProfile = findViewById(R.id.btn_edit_profile);
        btnSignOut = findViewById(R.id.btn_sign_out);
        adminBadge = findViewById(R.id.admin_badge);
        ivProfilePicture = findViewById(R.id.iv_user_profile_picture);
        rlImageContainer = findViewById(R.id.rl_profile_image_container);
    }

    private void setupNavigation() {
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
                    byte[] decodedString = Base64.decode(currentUser.getProfileImageUrl(), Base64.DEFAULT);
                    com.bumptech.glide.Glide.with(this).asBitmap().load(decodedString).circleCrop().into(navUserImage);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                navUserImage.setVisibility(View.GONE);
                navUserInitial.setVisibility(View.VISIBLE);
                if (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty()) {
                    navUserInitial.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
                }
            }

            if (currentUser.admin) {
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_ulist);
                if (adminItem != null) adminItem.setVisible(true);
            }
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_info) {
            startActivity(new Intent(this, InfoActivity.class));
            finish();
        } else if (id == R.id.nav_product) {
            startActivity(new Intent(this, CatalogActivity.class));
            finish();
        } else if (id == R.id.nav_update) {
            if (!isCurrentUser) {
                startActivity(new Intent(this, UserProfileActivity.class));
                finish();
            }
        } else if (id == R.id.nav_ulist) {
            startActivity(new Intent(this, UsersListActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_edit_profile) {
            updateUserProfile();
        } else if (v.getId() == R.id.btn_sign_out) {
            showLogoutDialog();
        }
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
            alertDialog.getWindow().setDimAmount(0.7f); // העליתי ל-0.7 כדי שיהיה רקע כהה וחזק
        }

        alertDialog.show();
    }

    private void showUserProfile() {
        databaseService.getUser(selectedUid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) return;
                selectedUser = user;
                etUserFirstName.setText(user.getFirstName());
                etUserLastName.setText(user.getLastName());
                etUserEmail.setText(user.getEmail());
                etUserPhone.setText(user.getPhone());
                etUserPassword.setText(user.getPassword());
                tvUserDisplayName.setText(user.getFullName());
                tvUserDisplayEmail.setText(user.getEmail());
                adminBadge.setVisibility(user.admin ? View.VISIBLE : View.GONE);

                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(user.getProfileImageUrl(), Base64.DEFAULT);
                        com.bumptech.glide.Glide.with(UserProfileActivity.this)
                                .asBitmap()
                                .load(decodedString)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .circleCrop()
                                .into(ivProfilePicture);
                    } catch (Exception e) {
                        Log.e(TAG, "Error decoding image", e);
                    }
                }
            }
            @Override
            public void onFailed(Exception e) { Log.e(TAG, "Error", e); }
        });
        if (!isCurrentUser) {
            etUserEmail.setEnabled(false);
            etUserPassword.setEnabled(false);
        }
    }

    private void updateUserProfile() {
        if (selectedUser == null) return;
        String firstName = etUserFirstName.getText().toString().trim();
        String lastName = etUserLastName.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String password = etUserPassword.getText().toString().trim();

        if (!isValid(firstName, lastName, phone, email, password)) return;

        selectedUser.setFirstName(firstName);
        selectedUser.setLastName(lastName);
        selectedUser.setPhone(phone);
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);

        updateUserInDatabase(selectedUser);
        if (selectedImageUri != null) { uploadImageAndSave(); }
    }

    private void updateUserInDatabase(User user) {
        databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן", Toast.LENGTH_SHORT).show();
                if (isCurrentUser) { SharedPreferencesUtil.saveUser(UserProfileActivity.this, user); }
                showUserProfile();
            }
            @Override
            public void onFailed(Exception e) { Toast.makeText(UserProfileActivity.this, "נכשל", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void uploadImageAndSave() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            databaseService.uploadProfilePictureAsBase64(selectedUid, bitmap, new DatabaseService.DatabaseCallback<String>() {
                @Override
                public void onCompleted(String base64Image) {
                    Toast.makeText(UserProfileActivity.this, "התמונה עודכנה", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null;
                }
                @Override
                public void onFailed(Exception e) { Log.e(TAG, "Failed", e); }
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean isValid(String f, String l, String ph, String em, String ps) {
        if (!Validator.isEmailValid(em)) { etUserEmail.setError("אימייל לא תקין"); return false; }
        if (!Validator.isNameValid(f)) { etUserFirstName.setError("קצר מדי"); return false; }
        if (!Validator.isNameValid(l)) { etUserLastName.setError("קצר מדי"); return false; }
        if (!Validator.isPhoneValid(ph)) { etUserPhone.setError("טלפון לא תקין"); return false; }
        return true;
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