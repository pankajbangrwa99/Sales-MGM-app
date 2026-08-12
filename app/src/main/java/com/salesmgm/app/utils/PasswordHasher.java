package com.salesmgm.app.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String SALT = "SalesMgmAppSecureSalt2026";

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((rawPassword + SALT).getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return rawPassword;
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        String newHash = hashPassword(rawPassword);
        return storedHash.equals(newHash);
    }
}
