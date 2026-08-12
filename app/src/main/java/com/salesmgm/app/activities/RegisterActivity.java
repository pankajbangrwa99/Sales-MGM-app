package com.salesmgm.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.utils.SessionManager;
import com.salesmgm.app.utils.ToastUtils;

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_REGISTERED_USERNAME = "registered_username";
    public static final String EXTRA_REGISTERED_PASSWORD = "registered_password";

    private EditText etFullName, etUsername, etPassword, etConfirmPassword;
    private Spinner spinnerRole;
    private Button btnRegisterSubmit;
    private TextView tvBackToLogin;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etFullName = findViewById(R.id.etRegisterFullName);
        etUsername = findViewById(R.id.etRegisterUsername);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRegisterRole);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Populate Role spinner
        String[] roles = new String[]{"ADMIN (Full Access)", "STAFF (Billing & Inventory)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnRegisterSubmit.setOnClickListener(v -> performRegistration());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String selectedRoleItem = spinnerRole.getSelectedItem().toString();
        String role = selectedRoleItem.startsWith("ADMIN") ? "ADMIN" : "STAFF";

        if (fullName.isEmpty()) {
            ToastUtils.showToast(this, "Please enter your Full Name / Shop Name");
            return;
        }

        if (username.isEmpty()) {
            ToastUtils.showToast(this, "Please enter a Username");
            return;
        }

        if (username.length() < 3) {
            ToastUtils.showToast(this, "Username must be at least 3 characters");
            return;
        }

        if (password.isEmpty()) {
            ToastUtils.showToast(this, "Please enter a Password");
            return;
        }

        if (password.length() < 4) {
            ToastUtils.showToast(this, "Password must be at least 4 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            ToastUtils.showToast(this, "Passwords do not match!");
            return;
        }

        if (dbHelper.isUsernameTaken(username)) {
            ToastUtils.showToast(this, "Username '" + username + "' is already registered! Try another username.");
            return;
        }

        boolean success = dbHelper.registerUser(username, password, fullName, role);
        if (success) {
            showCredentialCreatedDialog(fullName, username, password, role);
        } else {
            ToastUtils.showToast(this, "Registration failed. Please try again.");
        }
    }

    private void showCredentialCreatedDialog(String fullName, String username, String password, String role) {
        String credentialDetails = "Account Created Successfully!\n\n" +
                "👤 Full Name: " + fullName + "\n" +
                "🔑 Username: " + username + "\n" +
                "🔐 Password: " + password + "\n" +
                "🏷️ Role: " + role + "\n\n" +
                "Your login credentials are saved in the local database.";

        new AlertDialog.Builder(this)
                .setTitle("First-Time User Credentials")
                .setMessage(credentialDetails)
                .setCancelable(false)
                .setPositiveButton("SIGN IN & START", (dialog, which) -> {
                    sessionManager.createLoginSession(username, fullName, role);
                    ToastUtils.showToast(RegisterActivity.this, "Welcome, " + fullName + "!");
                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNeutralButton("COPY & GO TO LOGIN", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("App Credentials", "Username: " + username + "\nPassword: " + password);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        ToastUtils.showToast(RegisterActivity.this, "Credentials copied to clipboard!");
                    }
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_REGISTERED_USERNAME, username);
                    resultIntent.putExtra(EXTRA_REGISTERED_PASSWORD, password);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .show();
    }
}
