package com.example.ftherapy.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
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
import com.example.ftherapy.utils.Validator;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button toRegFromLog;


    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvLogin;
    DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        // Get views from XML
        etEmail = findViewById(R.id.email_login);
        etPassword = findViewById(R.id.password_login);
        btnLogin = findViewById(R.id.button_login_to_main);
        tvLogin = findViewById(R.id.textView_login);
        toRegFromLog = findViewById(R.id.button_login_to_register);

        // Set up Listeners
        btnLogin.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        toRegFromLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            Log.d(TAG, "onClick: Login button clicked");

            // Get email and password entered by user
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Log email and password
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Password length: " + password.length());

            Log.d(TAG, "onClick: Validating input...");
            // Validate input
            if (!checkInput(email, password)) {
                // Stop if input is invalid
                Log.d(TAG, "onClick: Input validation failed");
                return;
            }

            Log.d(TAG, "onClick: Input validation passed");
            Log.d(TAG, "onClick: Logging in user...");

            // Show loading message
            Toast.makeText(this, "מתחבר", Toast.LENGTH_SHORT).show();

            // Login user
            loginUser(email, password);
        } else if (v.getId() == tvLogin.getId()) {
            // Navigate to Register Activity
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        }
    }

    // Method to check if input is valid
    private boolean checkInput(String email, String password) {
        if (!Validator.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email address");
            // Show error message to user
            etEmail.setError("Invalid email address");
            // Set focus to email field
            etEmail.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            Log.e(TAG, "checkInput: Invalid password");
            // Show error message to user
            etPassword.setError("Password must be at least 6 characters long");
            // Set focus to password field
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        databaseService.getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    Log.d(TAG, "onCompleted: User found and logged in: " + user.toString());

                    SharedPreferencesUtil.saveUser(LoginActivity.this, user);

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                } else {
                    Log.d(TAG, "onCompleted: User not found");
                    Toast.makeText(LoginActivity.this, "אימייל או סיסמה שגויים", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                Toast.makeText(LoginActivity.this, "שגיאה בחיבור למסד הנתונים", Toast.LENGTH_SHORT).show();
                SharedPreferencesUtil.signOutUser(LoginActivity.this);
            }
        });

        Log.d(TAG, "loginUser: getUserByEmailAndPassword called");
    }
}