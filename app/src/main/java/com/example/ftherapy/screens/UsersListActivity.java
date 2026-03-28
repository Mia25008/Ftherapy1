package com.example.ftherapy.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // הוספתי
import android.widget.Button; // הוספתי
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftherapy.R;
import com.example.ftherapy.adapters.UserAdapter;
import com.example.ftherapy.models.User;
import com.example.ftherapy.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends BaseActivity {

    private static final String TAG = "UsersListActivity";
    private UserAdapter userAdapter;
    private TextView tvUserCount;
    private SearchView searchView;
    private List<User> fullUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        btnAdmin.setOnClickListener(v -> {updateAdminStatus(user);
            alertDialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {confirmDeleteUser(user);
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
                onResume();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersListActivity.this, "עדכון נכשל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("האם את בטוחה שברצונך למחוק את " + user.getFullName() + "?")
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
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }
}