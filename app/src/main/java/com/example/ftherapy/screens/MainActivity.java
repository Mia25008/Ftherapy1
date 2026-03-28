package com.example.ftherapy.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ftherapy.R;
import com.example.ftherapy.models.User;
import com.example.ftherapy.services.DatabaseService;
import com.example.ftherapy.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button userUpdate, btn_logout, btn_ulist, btn_info, btn_product;
    private DatabaseService databaseService;
    String selectedUserid;
    boolean isCurrentUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userUpdate = findViewById(R.id.button_main_to_update);
        btn_logout = findViewById(R.id.button_main_logout);
        btn_ulist = findViewById(R.id.button_main_to_Ulist);
        btn_info = findViewById(R.id.button_main_to_info);
        btn_product = findViewById(R.id.button_main_to_product);

        User currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser == null) {
            Log.e(TAG, "No user found in SharedPreferences, redirecting...");
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "Current user admin status: " + currentUser.admin);
        if (currentUser.isAdmin()) {
            btn_ulist.setVisibility(View.VISIBLE);
        }

        selectedUserid = getIntent().getStringExtra("USER_UID");
        if (selectedUserid == null) {
            selectedUserid = currentUser.getId();
        }
        isCurrentUser = selectedUserid.equals(currentUser.getId());


        userUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        btn_ulist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UsersListActivity.class);
                startActivity(intent);
            }
        });

        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
            }
        });

        btn_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CatalogActivity.class));
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });
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
}