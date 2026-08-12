package com.salesmgm.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.salesmgm.app.R;
import com.salesmgm.app.database.DatabaseHelper;
import com.salesmgm.app.utils.SessionManager;
import com.salesmgm.app.utils.ShopSettingsManager;

public class ShopSettingsActivity extends AppCompatActivity {

    private EditText etProfileFullName, etProfileOwnerName, etProfileEmail, etProfilePhone;
    private EditText etSettingsShopName, etSettingsBillingDesc, etSettingsBankDetails;
    private EditText etSettingsNewUsername, etSettingsNewPassword;
    private Button btnSaveProfile, btnSaveShopInfo, btnUpdateCredentials;

    private ShopSettingsManager settingsManager;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_settings);

        settingsManager = new ShopSettingsManager(this);
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etProfileFullName = findViewById(R.id.etProfileFullName);
        etProfileOwnerName = findViewById(R.id.etProfileOwnerName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        etSettingsShopName = findViewById(R.id.etSettingsShopName);
        etSettingsBillingDesc = findViewById(R.id.etSettingsBillingDesc);
        etSettingsBankDetails = findViewById(R.id.etSettingsBankDetails);
        etSettingsNewUsername = findViewById(R.id.etSettingsNewUsername);
        etSettingsNewPassword = findViewById(R.id.etSettingsNewPassword);

        btnSaveShopInfo = findViewById(R.id.btnSaveShopInfo);
        btnUpdateCredentials = findViewById(R.id.btnUpdateCredentials);

        // Pre-fill existing settings & profile
        etProfileFullName.setText(settingsManager.getProfileFullName().isEmpty() ? sessionManager.getFullName() : settingsManager.getProfileFullName());
        etProfileOwnerName.setText(settingsManager.getShopOwnerName());
        etProfileEmail.setText(settingsManager.getProfileEmail());
        etProfilePhone.setText(settingsManager.getProfilePhone());

        etSettingsShopName.setText(settingsManager.getShopName());
        etSettingsBillingDesc.setText(settingsManager.getBillingDescription());
        etSettingsBankDetails.setText(settingsManager.getBankDetails());
        etSettingsNewUsername.setText(sessionManager.getUsername());

        btnSaveProfile.setOnClickListener(v -> saveProfileDetails());
        btnSaveShopInfo.setOnClickListener(v -> saveStoreBranding());
        btnUpdateCredentials.setOnClickListener(v -> updateCredentials());
    }

    private void saveProfileDetails() {
        String fullName = etProfileFullName.getText().toString().trim();
        String ownerName = etProfileOwnerName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }

        settingsManager.setProfileFullName(fullName);
        settingsManager.setShopOwnerName(ownerName);
        settingsManager.setProfileEmail(email);
        settingsManager.setProfilePhone(phone);

        sessionManager.createLoginSession(sessionManager.getUsername(), fullName, sessionManager.getRole());

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void saveStoreBranding() {
        String name = etSettingsShopName.getText().toString().trim();
        String desc = etSettingsBillingDesc.getText().toString().trim();
        String bank = etSettingsBankDetails.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a shop name", Toast.LENGTH_SHORT).show();
            return;
        }

        settingsManager.setShopName(name);
        settingsManager.setBillingDescription(desc);
        settingsManager.setBankDetails(bank);

        Toast.makeText(this, "Store branding & PDF settings updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateCredentials() {
        String currentUsername = sessionManager.getUsername();
        String newUsername = etSettingsNewUsername.getText().toString().trim();
        String newPassword = etSettingsNewPassword.getText().toString().trim();

        if (newUsername.isEmpty() && newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new username or password to update", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateUserCredentials(currentUsername, newUsername, newPassword, null);
        if (success) {
            if (!newUsername.isEmpty()) {
                sessionManager.createLoginSession(newUsername, sessionManager.getFullName(), sessionManager.getRole());
            }
            Toast.makeText(this, "Login credentials updated successfully!", Toast.LENGTH_LONG).show();
            etSettingsNewPassword.setText("");
        } else {
            Toast.makeText(this, "Failed to update credentials. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
