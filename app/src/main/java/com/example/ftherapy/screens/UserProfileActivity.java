package com.example.ftherapy.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ftherapy.R;
import com.example.ftherapy.models.User;
import com.example.ftherapy.services.DatabaseService;
import com.example.ftherapy.utils.SharedPreferencesUtil;
import com.example.ftherapy.utils.Validator;

import java.util.List;

public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "UserProfileActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_user);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        selectedUid = getIntent().getStringExtra("USER_UID");
        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser == null) {
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_edit_profile) {
            updateUserProfile();
        } else if (v.getId() == R.id.btn_sign_out) {
            signOut();
        }
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
                    com.bumptech.glide.Glide.with(UserProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .circleCrop()
                            .into(ivProfilePicture);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
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

        if (selectedImageUri != null) {
            uploadImageAndSave();
        }
    }

    private void updateUserInDatabase(User user) {
        databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(UserProfileActivity.this, "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                // אם המשתמש עדכן את עצמו, נעדכן גם את ה-SharedPrefs
                if (isCurrentUser) {
                    SharedPreferencesUtil.saveUser(UserProfileActivity.this, user);
                }
                showUserProfile();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UserProfileActivity.this, "עדכון נכשל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageAndSave() {
        databaseService.uploadProfilePicture(selectedUid, selectedImageUri, new DatabaseService.DatabaseCallback<String>() {
            @Override
            public void onCompleted(String downloadUrl) {
                Toast.makeText(UserProfileActivity.this, "התמונה עודכנה", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(UserProfileActivity.this, "העלאת תמונה נכשלה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid(String firstName, String lastName, String phone, String email, String password) {
        if (!Validator.isEmailValid(email)) { etUserEmail.setError("אימייל לא תקין"); return false; }
        if (!Validator.isNameValid(firstName)) { etUserFirstName.setError("שם פרטי קצר מדי"); return false; }
        if (!Validator.isNameValid(lastName)) { etUserLastName.setError("שם משפחה קצר מדי"); return false; }
        if (!Validator.isPhoneValid(phone)) { etUserPhone.setError("טלפון לא תקין"); return false; }
        if (!Validator.isPasswordValid(password)) { etUserPassword.setError("סיסמה חלשה"); return false; }
        return true;
    }

    private void signOut() {
        SharedPreferencesUtil.signOutUser(this);
        Intent intent = new Intent(this, LandingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}