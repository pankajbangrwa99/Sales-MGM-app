package com.salesmgm.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ShopSettingsManager {

    private static final String PREF_NAME = "shop_settings_pref";
    private static final String KEY_SHOP_NAME = "key_shop_name";
    private static final String KEY_BILLING_DESC = "key_billing_desc";
    private static final String KEY_BANK_DETAILS = "key_bank_details";
    private static final String KEY_PROFILE_FULL_NAME = "key_profile_full_name";
    private static final String KEY_SHOP_OWNER_NAME = "key_shop_owner_name";
    private static final String KEY_PROFILE_EMAIL = "key_profile_email";
    private static final String KEY_PROFILE_PHONE = "key_profile_phone";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public ShopSettingsManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public String getShopName() {
        return pref.getString(KEY_SHOP_NAME, "Retail Sales & Super Mart");
    }

    public void setShopName(String shopName) {
        editor.putString(KEY_SHOP_NAME, shopName);
        editor.apply();
    }

    public String getBillingDescription() {
        return pref.getString(KEY_BILLING_DESC, "Offline Billing & POS Management System");
    }

    public void setBillingDescription(String desc) {
        editor.putString(KEY_BILLING_DESC, desc);
        editor.apply();
    }

    public String getBankDetails() {
        return pref.getString(KEY_BANK_DETAILS, "Bank: HDFC Bank | A/C: 987654321012 | IFSC: HDFC0001234 | UPI: shopkeeper@upi");
    }

    public void setBankDetails(String bankDetails) {
        editor.putString(KEY_BANK_DETAILS, bankDetails);
        editor.apply();
    }

    public String getProfileFullName() {
        return pref.getString(KEY_PROFILE_FULL_NAME, "");
    }

    public void setProfileFullName(String name) {
        editor.putString(KEY_PROFILE_FULL_NAME, name);
        editor.apply();
    }

    public String getShopOwnerName() {
        return pref.getString(KEY_SHOP_OWNER_NAME, "");
    }

    public void setShopOwnerName(String ownerName) {
        editor.putString(KEY_SHOP_OWNER_NAME, ownerName);
        editor.apply();
    }

    public String getProfileEmail() {
        return pref.getString(KEY_PROFILE_EMAIL, "");
    }

    public void setProfileEmail(String email) {
        editor.putString(KEY_PROFILE_EMAIL, email);
        editor.apply();
    }

    public String getProfilePhone() {
        return pref.getString(KEY_PROFILE_PHONE, "");
    }

    public void setProfilePhone(String phone) {
        editor.putString(KEY_PROFILE_PHONE, phone);
        editor.apply();
    }
}
