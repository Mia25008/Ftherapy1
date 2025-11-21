package com.example.ftherapy.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.ftherapy.utils.ValidatorActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    Button toLog;
    Button toMain;

    private EditText etEmail, etPassword, etFName, etLName, etPhone, etAge;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// get the views
        etFName = findViewById(R.id.first_name_register);
        etLName = findViewById(R.id.last_name_register);
        etEmail = findViewById(R.id.email_register);
        etPassword = findViewById(R.id.password_register);
        etPhone = findViewById(R.id.phone_register);
        etAge = findViewById(R.id.age_register);

        btnRegister = findViewById(R.id.button_register_to_main);
        btnRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) {
            Log.d(TAG, "onClick: Register button clicked");

            /// get the input from the user
            String fName = etFName.getText().toString();
            String lName = etLName.getText().toString();
            String password = etPassword.getText().toString();
            String email = etEmail.getText().toString();
            String phone = etPhone.getText().toString();
            String age = etAge.getText().toString();

            /// log the input
            Log.d(TAG, "onClick: First Name: " + fName);
            Log.d(TAG, "onClick: Last Name: " + lName);
            Log.d(TAG, "onClick: Password: " + password);
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Phone: " + phone);
            Log.d(TAG, "onClick: Age: " + age);


            /// Validate input
            Log.d(TAG, "onClick: Validating input...");
            if (!checkInput(email, password, fName, lName, phone, age)){
                /// stop if input is invalid
                return;
            }

            Log.d(TAG, "onClick: Registering user...");

            /// Register email, password, fName, lName, phone, age);
        } else if (v.getId() == tvLogin.getId()) {
            /// Navigate back to Login Activity
            finish();
        }
    }

    /// Check if the input is valid
    /// @return true if the input is valid, false otherwise
    /// @see Validator
    private boolean checkInput(String email, String password, String fName, String lName, String phone, String age) {

        if (!ValidatorActivity.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email address");
            /// show error message to user
            etEmail.setError("Invalid email address");
            /// set focus to email field
            etEmail.requestFocus();
            return false;
        }

        if (!ValidatorActivity.isPasswordValid(password)) {
            Log.e(TAG, "checkInput: Password must be at least 6 characters long");
            /// show error message to user
            etPassword.setError("Password must be at least 6 characters long");
            /// set focus to password field
            etPassword.requestFocus();
            return false;
        }

        if (!ValidatorActivity.isNameValid(fName)) {
            Log.e(TAG, "checkInput: First name must be at least 3 characters long");
            /// show error message to user
            etFName.setError("First name must be at least 3 characters long");
            /// set focus to first name field
            etFName.requestFocus();
            return false;
        }


        if (!ValidatorActivity.isNameValid(lName)) {
            Log.e(TAG, "checkInput: Last name must be at least 3 characters long");
            /// show error message to user
            etLName.setError("Last name must be at least 3 characters long");
            /// set focus to last name field
            etLName.requestFocus();
            return false;
        }

        if (!ValidatorActivity.isPhoneValid(phone)) {
            Log.e(TAG, "checkInput: Phone number must be at least 10 characters long");
            /// show error message to user
            etPhone.setError("Phone number must be at least 10 characters long");
            /// set focus to phone field
            etPhone.requestFocus();
            return false;
        }

        if (!ValidatorActivity.isAgeValid(age)) {
            Log.e(TAG, "checkInput: Age must be 2 digit long");
            /// show error message to user
            etAge.setError("Phone number must be 2 digit long");
            /// set focus to phone field
            etAge.requestFocus();
            return false;
        }

        Log.d(TAG, "checkInput: Input is valid");
        return true;
    }

    /// Register the user
    private void registerUser(String email, String password, String fName, String lName, String phone, String age) {
        Log.d(TAG, "registerUser: Registering user...");

        DatabaseService databaseService = DatabaseService.getInstance();
        String uid = databaseService.generateUserId();


        /// create a new user object
        User user = new User(uid, fName, lName, email, password, false, phone, age);

        databaseService.checkIfEmailExists(email, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    Log.e(TAG, "onCompleted: Email already exists");
                    /// show error message to user
                    Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                } else {
                    /// proceed to create the user
                    createUserInDatabase(user);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to check if email exists", e);
                /// show error message to user
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        DatabaseService databaseService = DatabaseService.getInstance();
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d(TAG, "createUserInDatabase: User created successfully");
                /// save the user to shared preferences
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);
                Log.d(TAG, "createUserInDatabase: Redirecting to MainActivity");
                /// Redirect to MainActivity and clear back stack to prevent user from going back to register screen
                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                /// clear the back stack (clear history) and start the MainActivity
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                /// show error message to user
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                /// sign out the user if failed to register
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
            }
        });
    }
}