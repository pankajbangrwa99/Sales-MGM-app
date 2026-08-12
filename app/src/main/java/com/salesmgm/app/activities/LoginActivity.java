package com.salesmgm.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.models.User;
import com.salesmgm.app.utils.PasswordHasher;
import com.salesmgm.app.utils.PermissionUtils;
import com.salesmgm.app.utils.SessionManager;
import com.salesmgm.app.utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REGISTER = 101;

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> performLogin());
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_REGISTER);
        });

        // Check & request Notification and SMS permissions on app startup
        PermissionUtils.checkAndRequestPermissions(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_REGISTER && resultCode == RESULT_OK && data != null) {
            String registeredUsername = data.getStringExtra(RegisterActivity.EXTRA_REGISTERED_USERNAME);
            String registeredPassword = data.getStringExtra(RegisterActivity.EXTRA_REGISTERED_PASSWORD);

            if (registeredUsername != null) etUsername.setText(registeredUsername);
            if (registeredPassword != null) etPassword.setText(registeredPassword);

            ToastUtils.showToast(this, "Credentials auto-filled! Tap SIGN IN TO DASHBOARD.");
        }
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            ToastUtils.showToast(this, "Please enter both username and password");
            return;
        }

        User user = dbHelper.getUserByUsername(username);
        if (user != null && PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            sessionManager.createLoginSession(user.getUsername(), user.getFullName(), user.getRole());
            ToastUtils.showToast(this, "Welcome back, " + user.getFullName());
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            ToastUtils.showToast(this, "Invalid username or password. Please try again.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
