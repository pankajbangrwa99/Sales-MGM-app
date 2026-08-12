# Sales & Stock Management App — Installation & User Guide

Welcome to the **Sales & Stock Management App** built for local shopkeepers and retail businesses. This guide provides comprehensive, step-by-step instructions to build the app, generate the Android APK file, transfer and install it on an Android mobile phone, and grant necessary device permissions.

---

## Table of Contents

1. [Key App Features](#1-key-app-features)
2. [Default Credentials](#2-default-credentials)
3. [Building the App & Generating APK](#3-building-the-app--generating-apk)
   - [Method A: Using Android Studio (Recommended)](#method-a-using-android-studio-recommended)
   - [Method B: Using Gradle Command Line](#method-b-using-gradle-command-line)
4. [Installing APK on Android Mobile Phone](#4-installing-apk-on-android-mobile-phone)
   - [Step 1: Enable Unknown App Installation](#step-1-enable-unknown-app-installation)
   - [Step 2: Transfer APK to Mobile Phone](#step-2-transfer-apk-to-mobile-phone)
   - [Step 3: Install & Launch](#step-3-install--launch)
5. [Permissions & Device Setup](#5-permissions--device-setup)
6. [App Modules & Features Walkthrough](#6-app-modules--features-walkthrough)

---

## 1. Key App Features

- ⚡ **Instant Billing System**: Fast POS cart checkout, automatic price calculations, cash/online/credit payment modes.
- 📄 **Native PDF Invoices**: Generates professional A4 invoice PDFs stored locally in phone storage.
- 📦 **Stock & Inventory Control**: Real-time product stock tracking with visual **Low Stock Warnings** (for products with quantity < 4).
- 👥 **Customer Khata / Credit Tracking**: Maintain customer credit balances, purchase histories, and payment logs.
- 📩 **Device SIM SMS Reminders**: Send credit reminder SMS directly using your phone's SIM card — **zero external API fees**.
- 🔒 **Secure Local Offline Database**: Complete SQLite database with salted password hashing.

---

## 2. Default Credentials

When opening the app for the first time, use these default admin credentials:

- **Username**: `admin`
- **Password**: `admin123`

---

## 3. Building the App & Generating APK

### Method A: Using Android Studio (Recommended)

1. Open **Android Studio**.
2. Click **File > Open...** and select the project folder:
   `C:\Users\Ripudaman\Documents\Sales Mgm App`
3. Allow Android Studio to complete the initial Gradle Sync.
4. From the top menu bar, navigate to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Once build completes, click **locate** in the popup notification.
   - Output Path: `app/build/outputs/apk/debug/app-debug.apk`

---

### Method B: Using Gradle Command Line

1. Open **PowerShell** or **Command Prompt** in the project root directory:
   ```bash
   cd "C:\Users\Ripudaman\Documents\Sales Mgm App"
   ```
2. Run the Gradle build command:
   ```bash
   ./gradlew assembleDebug
   ```
   *(Or using system Gradle: `gradle assembleDebug`)*
3. The compiled debug APK will be created at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Installing APK on Android Mobile Phone

### Step 1: Enable Unknown App Installation

To install an APK outside the Google Play Store:
1. Open **Settings** on your Android mobile phone.
2. Go to **Security & Privacy** (or **Apps > Special App Access**).
3. Tap **Install Unknown Apps**.
4. Select your **File Manager** app (or Chrome/WhatsApp if transferred through messaging) and toggle **Allow from this source** to **ON**.

---

### Step 2: Transfer APK to Mobile Phone

Choose any of the following transfer options:

#### Option 1: USB Cable (Easiest)
1. Connect your phone to your PC using a USB cable.
2. On your phone, select **File Transfer / MTP** mode from the notification panel.
3. Copy `app-debug.apk` from `app/build/outputs/apk/debug/` on your PC to the **Download** folder on your phone.

#### Option 2: ADB Command Line
If USB Debugging is enabled on your phone:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Option 3: Wireless Transfer (WhatsApp / Google Drive / Email)
1. Upload `app-debug.apk` to Google Drive or send it via WhatsApp / Email.
2. Download the file on your mobile phone.

---

### Step 3: Install & Launch

1. Open the **Files / File Manager** app on your phone.
2. Go to the **Downloads** folder and tap on `app-debug.apk`.
3. Tap **Install** when prompted.
4. Once installed, tap **Open** to launch **Sales & Stock Manager**.

---

## 5. Permissions & Device Setup

For full feature functionality on your mobile device:

1. **SMS Permission (`SEND_SMS`)**:
   - Required for sending payment reminders via SIM card.
   - When prompted upon sending your first SMS reminder, tap **Allow**.
   - You can also grant it manually under **Settings > Apps > Sales Management App > Permissions > SMS > Allow**.
2. **Storage Permission**:
   - Required to save PDF invoice files to device storage.

---

## 6. App Modules & Features Walkthrough

### 1. Dashboard
- View **Today's Sales**, **Total Revenue**, **Pending Credits**, and **Low Stock Warnings**.
- Quick buttons to start new billing, manage stock, view customers, and record payments.

### 2. Stock & Inventory Management
- Add, Edit, or Delete items (Name, Category, Price, Stock Quantity, Unit).
- Visual red warning badge displayed when quantity falls below 4 units.

### 3. Customer Management & Khata
- Add customer name, phone number, address, and initial credit balance.
- Tap **SMS Reminder** to dispatch an instant payment reminder message via SIM card.

### 4. Billing & POS Invoice System
- Select customer and add products to cart.
- Real-time stock availability validation.
- Select payment mode: **Cash**, **Online / UPI**, or **Credit (Khata)**.
- Generates **A4 PDF Invoices** stored locally, ready to view, share, or print.

---

*Project generated & ready for Android deployment.*
