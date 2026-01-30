package com.example.ftherapy.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


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

    Button userUpdate, btn_logout, btn_ulist, btn_info, btn_product;
    private DatabaseService databaseService;
    User selectedUser;
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

        selectedUserid = getIntent().getStringExtra("USER_UID");
        User currentUser = SharedPreferencesUtil.getUser(this);
        assert currentUser != null;

        if (btn_ulist == null) {
            selectedUserid = currentUser.getId();
        }
        isCurrentUser = selectedUserid.equals(currentUser.getId());
        btn_ulist  = findViewById(R.id.button_main_to_Ulist);

        if(currentUser.admin){
            btn_ulist.setVisibility(View.VISIBLE);
        } else {
            btn_ulist.setVisibility(View.GONE);
        }

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


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesUtil.signOutUser(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        btn_info = findViewById(R.id.button_main_to_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        btn_product = findViewById(R.id.button_main_to_product);
        btn_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
                startActivity(intent);
            }
        });
    }
}