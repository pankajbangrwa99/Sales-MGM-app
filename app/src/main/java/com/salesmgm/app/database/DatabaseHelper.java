package com.salesmgm.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.salesmgm.app.models.Bill;
import com.salesmgm.app.models.BillItem;
import com.salesmgm.app.models.Customer;
import com.salesmgm.app.models.Payment;
import com.salesmgm.app.models.Product;
import com.salesmgm.app.models.User;
import com.salesmgm.app.utils.PasswordHasher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sales_mgm.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_BILLS = "bills";
    private static final String TABLE_BILL_ITEMS = "bill_items";
    private static final String TABLE_PAYMENTS = "payments";
    private static final String TABLE_STOCK_LOGS = "stock_logs";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "full_name TEXT, " +
                "role TEXT)";
        db.execSQL(createUsersTable);

        // Products Table
        String createProductsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT, " +
                "price REAL NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "unit TEXT)";
        db.execSQL(createProductsTable);

        // Customers Table
        String createCustomersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOMERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT, " +
                "address TEXT, " +
                "credit_balance REAL DEFAULT 0.0)";
        db.execSQL(createCustomersTable);

        // Bills Table
        String createBillsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BILLS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "invoice_number TEXT UNIQUE NOT NULL, " +
                "customer_id INTEGER, " +
                "customer_name TEXT, " +
                "customer_phone TEXT, " +
                "total_amount REAL NOT NULL, " +
                "paid_amount REAL NOT NULL, " +
                "due_amount REAL NOT NULL, " +
                "payment_mode TEXT, " +
                "timestamp TEXT)";
        db.execSQL(createBillsTable);

        // Bill Items Table
        String createBillItemsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BILL_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bill_id INTEGER NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "product_name TEXT NOT NULL, " +
                "unit_price REAL NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "total_price REAL NOT NULL, " +
                "FOREIGN KEY(bill_id) REFERENCES " + TABLE_BILLS + "(id) ON DELETE CASCADE)";
        db.execSQL(createBillItemsTable);

        // Payments Table
        String createPaymentsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PAYMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "customer_name TEXT NOT NULL, " +
                "amount_paid REAL NOT NULL, " +
                "payment_mode TEXT, " +
                "note TEXT, " +
                "timestamp TEXT)";
        db.execSQL(createPaymentsTable);

        // Stock Logs Table
        String createStockLogsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "product_name TEXT, " +
                "movement_type TEXT, " +
                "quantity INTEGER, " +
                "previous_stock INTEGER, " +
                "new_stock INTEGER, " +
                "unit_price REAL, " +
                "total_amount REAL, " +
                "invoice_number TEXT, " +
                "customer_name TEXT, " +
                "payment_mode TEXT, " +
                "notes_supplier TEXT, " +
                "timestamp TEXT)";
        db.execSQL(createStockLogsTable);

        // Seed initial admin user if empty
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.getInt(0) == 0) {
                ContentValues adminValues = new ContentValues();
                adminValues.put("username", "admin");
                adminValues.put("password_hash", PasswordHasher.hashPassword("admin123"));
                adminValues.put("full_name", "Shop Owner Admin");
                adminValues.put("role", "ADMIN");
                db.insert(TABLE_USERS, null, adminValues);
            }
            cursor.close();
        }

        // Seed Sample Data only if tables are completely empty
        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        Cursor cProd = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        if (cProd != null && cProd.moveToFirst()) {
            if (cProd.getInt(0) == 0) {
                insertSampleProduct(db, "Basmati Rice (5kg)", "Grocery", 450.00, 15, "Bag");
                insertSampleProduct(db, "Sunflower Oil (1L)", "Grocery", 140.00, 3, "Ltr");
                insertSampleProduct(db, "Sugar (1kg)", "Grocery", 42.00, 25, "Kg");
                insertSampleProduct(db, "Tea Powder (250g)", "Beverages", 120.00, 2, "Pcs");
                insertSampleProduct(db, "Washing Powder (1kg)", "Household", 110.00, 10, "Pcs");
            }
            cProd.close();
        }

        Cursor cCust = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CUSTOMERS, null);
        if (cCust != null && cCust.moveToFirst()) {
            if (cCust.getInt(0) == 0) {
                insertSampleCustomer(db, "Ramesh Kumar", "9876543210", "Main Market, Street 4", 250.00);
                insertSampleCustomer(db, "Suresh Sharma", "9123456789", "Sector 12, Plot 45", 0.00);
                insertSampleCustomer(db, "Anita Verma", "9988776655", "Subhash Nagar", 1200.00);
            }
            cCust.close();
        }
    }

    private void insertSampleProduct(SQLiteDatabase db, String name, String category, double price, int qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("category", category);
        cv.put("price", price);
        cv.put("quantity", qty);
        cv.put("unit", unit);
        db.insert(TABLE_PRODUCTS, null, cv);
    }

    private void insertSampleCustomer(SQLiteDatabase db, String name, String phone, String address, double credit) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("address", address);
        cv.put("credit_balance", credit);
        db.insert(TABLE_CUSTOMERS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Safe Migration without dropping user data
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureStockLogsTable(db);
    }

    public void ensureStockLogsTable(SQLiteDatabase db) {
        String createStockLogsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "product_name TEXT, " +
                "movement_type TEXT, " +
                "quantity INTEGER, " +
                "previous_stock INTEGER, " +
                "new_stock INTEGER, " +
                "unit_price REAL, " +
                "total_amount REAL, " +
                "invoice_number TEXT, " +
                "customer_name TEXT, " +
                "payment_mode TEXT, " +
                "notes_supplier TEXT, " +
                "timestamp TEXT)";
        try {
            db.execSQL(createStockLogsTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- USER OPERATIONS ---
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "username = ?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                    cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role"))
            );
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean registerUser(String username, String rawPassword, String fullName, String role) {
        if (username == null || rawPassword == null || username.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            return false;
        }
        if (isUsernameTaken(username.trim())) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username.trim());
        cv.put("password_hash", PasswordHasher.hashPassword(rawPassword.trim()));
        cv.put("full_name", (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : username.trim());
        cv.put("role", (role != null && !role.trim().isEmpty()) ? role.trim() : "ADMIN");
        long id = db.insert(TABLE_USERS, null, cv);
        return id != -1;
    }

    public boolean isUsernameTaken(String username) {
        return getUserByUsername(username) != null;
    }

    public boolean updateUserCredentials(String currentUsername, String newUsername, String newPassword, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            cv.put("username", newUsername.trim());
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            cv.put("password_hash", PasswordHasher.hashPassword(newPassword.trim()));
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            cv.put("full_name", fullName.trim());
        }
        return db.update(TABLE_USERS, cv, "username = ?", new String[]{currentUsername}) > 0;
    }

    // --- PRODUCT OPERATIONS ---
    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("category", product.getCategory());
        values.put("price", product.getPrice());
        values.put("quantity", product.getQuantity());
        values.put("unit", product.getUnit());
        long id = db.insert(TABLE_PRODUCTS, null, values);

        if (id > 0) {
            try {
                ContentValues logCv = new ContentValues();
                logCv.put("product_id", id);
                logCv.put("product_name", product.getName());
                logCv.put("movement_type", "STOCK_IN");
                logCv.put("quantity", product.getQuantity());
                logCv.put("previous_stock", 0);
                logCv.put("new_stock", product.getQuantity());
                logCv.put("unit_price", product.getPrice());
                logCv.put("total_amount", product.getPrice() * product.getQuantity());
                logCv.put("notes_supplier", "Product Initial Addition");
                logCv.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                db.insert(TABLE_STOCK_LOGS, null, logCv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public boolean updateProduct(Product product) {
        Product old = getProductById(product.getId());
        int oldQty = (old != null) ? old.getQuantity() : 0;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("category", product.getCategory());
        values.put("price", product.getPrice());
        values.put("quantity", product.getQuantity());
        values.put("unit", product.getUnit());
        boolean updated = db.update(TABLE_PRODUCTS, values, "id = ?", new String[]{String.valueOf(product.getId())}) > 0;

        if (updated) {
            try {
                int diff = product.getQuantity() - oldQty;
                if (diff != 0) {
                    ContentValues logCv = new ContentValues();
                    logCv.put("product_id", product.getId());
                    logCv.put("product_name", product.getName());
                    logCv.put("movement_type", diff > 0 ? "STOCK_IN" : "ADJUSTMENT");
                    logCv.put("quantity", Math.abs(diff));
                    logCv.put("previous_stock", oldQty);
                    logCv.put("new_stock", product.getQuantity());
                    logCv.put("unit_price", product.getPrice());
                    logCv.put("total_amount", product.getPrice() * Math.abs(diff));
                    logCv.put("notes_supplier", diff > 0 ? "Inventory Restock / Update" : "Manual Stock Reduction");
                    logCv.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                    db.insert(TABLE_STOCK_LOGS, null, logCv);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return updated;
    }

    public boolean deleteProduct(long productId) {
        Product old = getProductById(productId);
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleted = db.delete(TABLE_PRODUCTS, "id = ?", new String[]{String.valueOf(productId)}) > 0;

        if (deleted && old != null) {
            try {
                ContentValues logCv = new ContentValues();
                logCv.put("product_id", old.getId());
                logCv.put("product_name", old.getName());
                logCv.put("movement_type", "ADJUSTMENT");
                logCv.put("quantity", old.getQuantity());
                logCv.put("previous_stock", old.getQuantity());
                logCv.put("new_stock", 0);
                logCv.put("unit_price", old.getPrice());
                logCv.put("total_amount", old.getPrice() * old.getQuantity());
                logCv.put("notes_supplier", "Product Removed from Inventory");
                logCv.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                db.insert(TABLE_STOCK_LOGS, null, logCv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deleted;
    }

    public Product getProductById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Product p = new Product(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("unit"))
            );
            cursor.close();
            return p;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Product> getAllProducts(String query) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (query != null && !query.trim().isEmpty()) {
            selection = "name LIKE ? OR category LIKE ?";
            selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        }

        Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, "name ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product p = new Product(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("unit"))
                );
                list.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, "quantity < 4", null, null, null, "quantity ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product p = new Product(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("unit"))
                );
                list.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- CUSTOMER OPERATIONS ---
    public long addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", customer.getName());
        values.put("phone", customer.getPhone());
        values.put("address", customer.getAddress());
        values.put("credit_balance", customer.getCreditBalance());
        return db.insert(TABLE_CUSTOMERS, null, values);
    }

    public boolean updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", customer.getName());
        values.put("phone", customer.getPhone());
        values.put("address", customer.getAddress());
        values.put("credit_balance", customer.getCreditBalance());
        return db.update(TABLE_CUSTOMERS, values, "id = ?", new String[]{String.valueOf(customer.getId())}) > 0;
    }

    public boolean deleteCustomer(long customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CUSTOMERS, "id = ?", new String[]{String.valueOf(customerId)}) > 0;
    }

    public boolean reinsertCustomer(Customer c) {
        if (c == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", c.getId());
        values.put("name", c.getName());
        values.put("phone", c.getPhone());
        values.put("address", c.getAddress());
        values.put("credit_balance", c.getCreditBalance());
        return db.insert(TABLE_CUSTOMERS, null, values) > 0;
    }

    public boolean reinsertProduct(Product p) {
        if (p == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", p.getId());
        values.put("name", p.getName());
        values.put("category", p.getCategory());
        values.put("price", p.getPrice());
        values.put("quantity", p.getQuantity());
        values.put("unit", p.getUnit());
        return db.insert(TABLE_PRODUCTS, null, values) > 0;
    }

    public Customer getCustomerById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Customer c = new Customer(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("credit_balance"))
            );
            cursor.close();
            return c;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Customer> getAllCustomers(String query) {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (query != null && !query.trim().isEmpty()) {
            selection = "name LIKE ? OR phone LIKE ?";
            selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        }

        Cursor cursor = db.query(TABLE_CUSTOMERS, null, selection, selectionArgs, null, null, "name ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Customer c = new Customer(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("credit_balance"))
                );
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Customer> getCustomersWithDueCredit() {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, "credit_balance > 0", null, null, null, "credit_balance DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Customer c = new Customer(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("credit_balance"))
                );
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- BILLING OPERATIONS ---
    public String getNextInvoiceNumber() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BILLS, null);
        long count = 0;
        if (countCursor != null && countCursor.moveToFirst()) {
            count = countCursor.getLong(0);
            countCursor.close();
        }

        if (count == 0) {
            try {
                db.execSQL("DELETE FROM sqlite_sequence WHERE name = ?", new String[]{TABLE_BILLS});
            } catch (Exception ignored) {}
            return "INV-1";
        }

        Cursor maxCursor = db.rawQuery("SELECT MAX(id) FROM " + TABLE_BILLS, null);
        long maxId = 0;
        if (maxCursor != null && maxCursor.moveToFirst()) {
            maxId = maxCursor.getLong(0);
            maxCursor.close();
        }
        long nextNumber = maxId + 1;
        return "INV-" + nextNumber;
    }

    public long createBill(Bill bill) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("invoice_number", bill.getInvoiceNumber());
            cv.put("customer_id", bill.getCustomerId());
            cv.put("customer_name", bill.getCustomerName());
            cv.put("customer_phone", bill.getCustomerPhone());
            cv.put("total_amount", bill.getTotalAmount());
            cv.put("paid_amount", bill.getPaidAmount());
            cv.put("due_amount", bill.getDueAmount());
            cv.put("payment_mode", bill.getPaymentMode());
            cv.put("timestamp", bill.getTimestamp());

            long billId = db.insert(TABLE_BILLS, null, cv);

            // Insert Items & Deduct Stock
            for (BillItem item : bill.getItems()) {
                ContentValues itemCv = new ContentValues();
                itemCv.put("bill_id", billId);
                itemCv.put("product_id", item.getProductId());
                itemCv.put("product_name", item.getProductName());
                itemCv.put("unit_price", item.getUnitPrice());
                itemCv.put("quantity", item.getQuantity());
                itemCv.put("total_price", item.getTotalPrice());
                db.insert(TABLE_BILL_ITEMS, null, itemCv);

                // Deduct stock quantity
                db.execSQL("UPDATE " + TABLE_PRODUCTS + " SET quantity = quantity - ? WHERE id = ?",
                        new Object[]{item.getQuantity(), item.getProductId()});

                // Record Stock Out Log
                try {
                    ContentValues logCv = new ContentValues();
                    logCv.put("product_id", item.getProductId());
                    logCv.put("product_name", item.getProductName());
                    logCv.put("movement_type", "STOCK_OUT");
                    logCv.put("quantity", item.getQuantity());
                    logCv.put("unit_price", item.getUnitPrice());
                    logCv.put("total_amount", item.getTotalPrice());
                    logCv.put("invoice_number", bill.getInvoiceNumber());
                    logCv.put("customer_name", bill.getCustomerName());
                    logCv.put("payment_mode", bill.getPaymentMode());
                    logCv.put("notes_supplier", "Sales Invoice Created (" + bill.getInvoiceNumber() + ")");
                    logCv.put("timestamp", bill.getTimestamp());
                    db.insert(TABLE_STOCK_LOGS, null, logCv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Update Customer Credit Balance if due amount > 0 and customer valid
            if (bill.getCustomerId() > 0 && bill.getDueAmount() > 0) {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = credit_balance + ? WHERE id = ?",
                        new Object[]{bill.getDueAmount(), bill.getCustomerId()});
            }

            db.setTransactionSuccessful();
            return billId;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteBill(long billId) {
        Bill bill = getBillById(billId);
        if (bill == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Revert product stock quantities & record return log
            if (bill.getItems() != null) {
                for (BillItem item : bill.getItems()) {
                    db.execSQL("UPDATE " + TABLE_PRODUCTS + " SET quantity = quantity + ? WHERE id = ?",
                            new Object[]{item.getQuantity(), item.getProductId()});

                    try {
                        ContentValues logCv = new ContentValues();
                        logCv.put("product_id", item.getProductId());
                        logCv.put("product_name", item.getProductName());
                        logCv.put("movement_type", "STOCK_IN");
                        logCv.put("quantity", item.getQuantity());
                        logCv.put("unit_price", item.getUnitPrice());
                        logCv.put("total_amount", item.getTotalPrice());
                        logCv.put("invoice_number", bill.getInvoiceNumber());
                        logCv.put("customer_name", bill.getCustomerName());
                        logCv.put("payment_mode", bill.getPaymentMode());
                        logCv.put("notes_supplier", "Invoice Deletion Return (" + bill.getInvoiceNumber() + ")");
                        logCv.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                        db.insert(TABLE_STOCK_LOGS, null, logCv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 2. Revert customer credit balance
            if (bill.getCustomerId() > 0 && bill.getDueAmount() > 0) {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = MAX(0, credit_balance - ?) WHERE id = ?",
                        new Object[]{bill.getDueAmount(), bill.getCustomerId()});
            }

            // 3. Delete items and bill
            db.delete(TABLE_BILL_ITEMS, "bill_id = ?", new String[]{String.valueOf(billId)});
            int rows = db.delete(TABLE_BILLS, "id = ?", new String[]{String.valueOf(billId)});

            Cursor checkCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BILLS, null);
            if (checkCursor != null && checkCursor.moveToFirst()) {
                if (checkCursor.getLong(0) == 0) {
                    try {
                        db.execSQL("DELETE FROM sqlite_sequence WHERE name = ?", new String[]{TABLE_BILLS});
                    } catch (Exception ignored) {}
                }
                checkCursor.close();
            }

            db.setTransactionSuccessful();
            return rows > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean reinsertBill(Bill bill) {
        if (bill == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("id", bill.getId());
            cv.put("invoice_number", bill.getInvoiceNumber());
            cv.put("customer_id", bill.getCustomerId());
            cv.put("customer_name", bill.getCustomerName());
            cv.put("customer_phone", bill.getCustomerPhone());
            cv.put("total_amount", bill.getTotalAmount());
            cv.put("paid_amount", bill.getPaidAmount());
            cv.put("due_amount", bill.getDueAmount());
            cv.put("payment_mode", bill.getPaymentMode());
            cv.put("timestamp", bill.getTimestamp());

            db.insert(TABLE_BILLS, null, cv);

            if (bill.getItems() != null) {
                for (BillItem item : bill.getItems()) {
                    ContentValues itemCv = new ContentValues();
                    itemCv.put("bill_id", bill.getId());
                    itemCv.put("product_id", item.getProductId());
                    itemCv.put("product_name", item.getProductName());
                    itemCv.put("unit_price", item.getUnitPrice());
                    itemCv.put("quantity", item.getQuantity());
                    itemCv.put("total_price", item.getTotalPrice());
                    db.insert(TABLE_BILL_ITEMS, null, itemCv);

                    // Re-deduct stock quantity
                    db.execSQL("UPDATE " + TABLE_PRODUCTS + " SET quantity = quantity - ? WHERE id = ?",
                            new Object[]{item.getQuantity(), item.getProductId()});
                }
            }

            // Re-apply Customer Credit Balance
            if (bill.getCustomerId() > 0 && bill.getDueAmount() > 0) {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = credit_balance + ? WHERE id = ?",
                        new Object[]{bill.getDueAmount(), bill.getCustomerId()});
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateBill(Bill updatedBill) {
        Bill oldBill = getBillById(updatedBill.getId());
        if (oldBill == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Revert old stock deduction
            if (oldBill.getItems() != null) {
                for (BillItem oldItem : oldBill.getItems()) {
                    db.execSQL("UPDATE " + TABLE_PRODUCTS + " SET quantity = quantity + ? WHERE id = ?",
                            new Object[]{oldItem.getQuantity(), oldItem.getProductId()});
                }
            }

            // 2. Revert old customer credit
            if (oldBill.getCustomerId() > 0 && oldBill.getDueAmount() > 0) {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = MAX(0, credit_balance - ?) WHERE id = ?",
                        new Object[]{oldBill.getDueAmount(), oldBill.getCustomerId()});
            }

            // 3. Delete old items
            db.delete(TABLE_BILL_ITEMS, "bill_id = ?", new String[]{String.valueOf(updatedBill.getId())});

            // 4. Update bill record
            ContentValues cv = new ContentValues();
            cv.put("customer_id", updatedBill.getCustomerId());
            cv.put("customer_name", updatedBill.getCustomerName());
            cv.put("customer_phone", updatedBill.getCustomerPhone());
            cv.put("total_amount", updatedBill.getTotalAmount());
            cv.put("paid_amount", updatedBill.getPaidAmount());
            cv.put("due_amount", updatedBill.getDueAmount());
            cv.put("payment_mode", updatedBill.getPaymentMode());
            db.update(TABLE_BILLS, cv, "id = ?", new String[]{String.valueOf(updatedBill.getId())});

            // 5. Insert new items & deduct new stock
            if (updatedBill.getItems() != null) {
                for (BillItem item : updatedBill.getItems()) {
                    ContentValues itemCv = new ContentValues();
                    itemCv.put("bill_id", updatedBill.getId());
                    itemCv.put("product_id", item.getProductId());
                    itemCv.put("product_name", item.getProductName());
                    itemCv.put("unit_price", item.getUnitPrice());
                    itemCv.put("quantity", item.getQuantity());
                    itemCv.put("total_price", item.getTotalPrice());
                    db.insert(TABLE_BILL_ITEMS, null, itemCv);

                    db.execSQL("UPDATE " + TABLE_PRODUCTS + " SET quantity = quantity - ? WHERE id = ?",
                            new Object[]{item.getQuantity(), item.getProductId()});
                }
            }

            // 6. Apply new customer credit balance
            if (updatedBill.getCustomerId() > 0 && updatedBill.getDueAmount() > 0) {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = credit_balance + ? WHERE id = ?",
                        new Object[]{updatedBill.getDueAmount(), updatedBill.getCustomerId()});
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BILLS, null, null, null, null, null, "id DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Bill bill = extractBillFromCursor(cursor);
                list.add(bill);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Bill getBillById(long billId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BILLS, null, "id = ?", new String[]{String.valueOf(billId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Bill bill = extractBillFromCursor(cursor);
            cursor.close();
            bill.setItems(getBillItems(billId));
            return bill;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<BillItem> getBillItems(long billId) {
        List<BillItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BILL_ITEMS, null, "bill_id = ?", new String[]{String.valueOf(billId)}, null, null, "id ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                BillItem item = new BillItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                item.setBillId(cursor.getLong(cursor.getColumnIndexOrThrow("bill_id")));
                item.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow("product_id")));
                item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                item.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price")));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                item.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")));
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private Bill extractBillFromCursor(Cursor cursor) {
        Bill bill = new Bill();
        bill.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        bill.setInvoiceNumber(cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")));
        bill.setCustomerId(cursor.getLong(cursor.getColumnIndexOrThrow("customer_id")));
        bill.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
        bill.setCustomerPhone(cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")));
        bill.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
        bill.setPaidAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("paid_amount")));
        bill.setDueAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("due_amount")));
        bill.setPaymentMode(cursor.getString(cursor.getColumnIndexOrThrow("payment_mode")));
        bill.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
        return bill;
    }

    // --- PAYMENT RECORDING & CREDIT MANAGEMENT ---
    public long recordPayment(long customerId, String customerName, double amount, String paymentMode, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            ContentValues cv = new ContentValues();
            cv.put("customer_id", customerId);
            cv.put("customer_name", customerName);
            cv.put("amount_paid", amount);
            cv.put("payment_mode", paymentMode);
            cv.put("note", note);
            cv.put("timestamp", timestamp);

            long paymentId = db.insert(TABLE_PAYMENTS, null, cv);

            // Deduct customer credit balance
            db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET credit_balance = MAX(0, credit_balance - ?) WHERE id = ?",
                    new Object[]{amount, customerId});

            db.setTransactionSuccessful();
            return paymentId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Payment> getPaymentsForCustomer(long customerId) {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PAYMENTS, null, "customer_id = ?", new String[]{String.valueOf(customerId)}, null, null, "id DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Payment p = new Payment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("customer_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount_paid")),
                        cursor.getString(cursor.getColumnIndexOrThrow("payment_mode")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                );
                list.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- DASHBOARD ANALYTICS ---
    public double getTotalSalesAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + TABLE_BILLS, null);
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public double getTodaySalesAmount() {
        String todayPrefix = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + TABLE_BILLS + " WHERE timestamp LIKE ?",
                new String[]{todayPrefix + "%"});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public double getMonthlySalesAmount() {
        String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total_amount) FROM " + TABLE_BILLS + " WHERE timestamp LIKE ?",
                new String[]{monthPrefix + "%"});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public double getTotalPendingCredit() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(credit_balance) FROM " + TABLE_CUSTOMERS, null);
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE quantity < 4", null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getTotalCustomersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CUSTOMERS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // --- STOCK LOG OPERATIONS ---
    public void syncHistoricalStockLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureStockLogsTable(db);
        try {
            Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_STOCK_LOGS, null);
            long count = 0;
            if (countCursor != null && countCursor.moveToFirst()) {
                count = countCursor.getLong(0);
                countCursor.close();
            }

            if (count == 0) {
                // Backfill from Bills
                List<Bill> bills = getAllBills();
                if (bills != null) {
                    for (Bill b : bills) {
                        if (b.getItems() != null) {
                            for (BillItem item : b.getItems()) {
                                ContentValues logCv = new ContentValues();
                                logCv.put("product_id", item.getProductId());
                                logCv.put("product_name", item.getProductName());
                                logCv.put("movement_type", "STOCK_OUT");
                                logCv.put("quantity", item.getQuantity());
                                logCv.put("unit_price", item.getUnitPrice());
                                logCv.put("total_amount", item.getTotalPrice());
                                logCv.put("invoice_number", b.getInvoiceNumber());
                                logCv.put("customer_name", b.getCustomerName());
                                logCv.put("payment_mode", b.getPaymentMode());
                                logCv.put("notes_supplier", "Sales Invoice (" + b.getInvoiceNumber() + ")");
                                logCv.put("timestamp", b.getTimestamp());
                                db.insert(TABLE_STOCK_LOGS, null, logCv);
                            }
                        }
                    }
                }

                // Backfill from Initial Products
                List<Product> products = getAllProducts(null);
                if (products != null) {
                    for (Product p : products) {
                        ContentValues logCv = new ContentValues();
                        logCv.put("product_id", p.getId());
                        logCv.put("product_name", p.getName());
                        logCv.put("movement_type", "STOCK_IN");
                        logCv.put("quantity", p.getQuantity());
                        logCv.put("previous_stock", 0);
                        logCv.put("new_stock", p.getQuantity());
                        logCv.put("unit_price", p.getPrice());
                        logCv.put("total_amount", p.getPrice() * p.getQuantity());
                        logCv.put("notes_supplier", "Initial Inventory Stock");
                        logCv.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
                        db.insert(TABLE_STOCK_LOGS, null, logCv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logStockMovement(com.salesmgm.app.models.StockLog log) {
        if (log == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ensureStockLogsTable(db);
        try {
            ContentValues cv = new ContentValues();
            cv.put("product_id", log.getProductId());
            cv.put("product_name", log.getProductName());
            cv.put("movement_type", log.getMovementType());
            cv.put("quantity", log.getQuantity());
            cv.put("previous_stock", log.getPreviousStock());
            cv.put("new_stock", log.getNewStock());
            cv.put("unit_price", log.getUnitPrice());
            cv.put("total_amount", log.getTotalAmount());
            cv.put("invoice_number", log.getInvoiceNumber());
            cv.put("customer_name", log.getCustomerName());
            cv.put("payment_mode", log.getPaymentMode());
            cv.put("notes_supplier", log.getNotesSupplier());
            cv.put("timestamp", log.getTimestamp());
            db.insert(TABLE_STOCK_LOGS, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<com.salesmgm.app.models.StockLog> getAllStockLogs(String query, String filterType) {
        List<com.salesmgm.app.models.StockLog> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        ensureStockLogsTable(db);

        try {
            String selection = null;
            List<String> selectionArgs = new ArrayList<>();

            if (filterType != null && !filterType.equalsIgnoreCase("ALL")) {
                selection = "movement_type = ?";
                selectionArgs.add(filterType);
            }

            if (query != null && !query.trim().isEmpty()) {
                if (selection == null) {
                    selection = "(product_name LIKE ? OR invoice_number LIKE ? OR customer_name LIKE ?)";
                } else {
                    selection += " AND (product_name LIKE ? OR invoice_number LIKE ? OR customer_name LIKE ?)";
                }
                selectionArgs.add("%" + query + "%");
                selectionArgs.add("%" + query + "%");
                selectionArgs.add("%" + query + "%");
            }

            String[] argsArray = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
            Cursor cursor = db.query(TABLE_STOCK_LOGS, null, selection, argsArray, null, null, "id DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    com.salesmgm.app.models.StockLog log = new com.salesmgm.app.models.StockLog();
                    log.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                    log.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow("product_id")));
                    log.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
                    log.setMovementType(cursor.getString(cursor.getColumnIndexOrThrow("movement_type")));
                    log.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    log.setPreviousStock(cursor.getInt(cursor.getColumnIndexOrThrow("previous_stock")));
                    log.setNewStock(cursor.getInt(cursor.getColumnIndexOrThrow("new_stock")));
                    log.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price")));
                    log.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
                    log.setInvoiceNumber(cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")));
                    log.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
                    log.setPaymentMode(cursor.getString(cursor.getColumnIndexOrThrow("payment_mode")));
                    log.setNotesSupplier(cursor.getString(cursor.getColumnIndexOrThrow("notes_supplier")));
                    log.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
                    list.add(log);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Dynamically augment from sales bills for complete history if stock_logs is clean
        if (list.isEmpty() && (filterType == null || filterType.equalsIgnoreCase("ALL") || filterType.equalsIgnoreCase("STOCK_OUT"))) {
            try {
                List<Bill> bills = getAllBills();
                if (bills != null) {
                    for (Bill b : bills) {
                        if (b.getItems() != null) {
                            for (BillItem item : b.getItems()) {
                                if (query != null && !query.trim().isEmpty()) {
                                    String q = query.toLowerCase();
                                    boolean match = (item.getProductName() != null && item.getProductName().toLowerCase().contains(q)) ||
                                                    (b.getInvoiceNumber() != null && b.getInvoiceNumber().toLowerCase().contains(q)) ||
                                                    (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(q));
                                    if (!match) continue;
                                }
                                com.salesmgm.app.models.StockLog log = new com.salesmgm.app.models.StockLog();
                                log.setId(item.getId());
                                log.setProductId(item.getProductId());
                                log.setProductName(item.getProductName());
                                log.setMovementType("STOCK_OUT");
                                log.setQuantity(item.getQuantity());
                                log.setUnitPrice(item.getUnitPrice());
                                log.setTotalAmount(item.getTotalPrice());
                                log.setInvoiceNumber(b.getInvoiceNumber());
                                log.setCustomerName(b.getCustomerName());
                                log.setPaymentMode(b.getPaymentMode());
                                log.setTimestamp(b.getTimestamp());
                                list.add(log);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public int getTotalStockInCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureStockLogsTable(db);
        int total = 0;
        try {
            Cursor cursor = db.rawQuery("SELECT SUM(quantity) FROM " + TABLE_STOCK_LOGS + " WHERE movement_type = 'STOCK_IN'", null);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public int getTotalStockOutCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureStockLogsTable(db);
        int total = 0;
        try {
            Cursor cursor = db.rawQuery("SELECT SUM(quantity) FROM " + TABLE_STOCK_LOGS + " WHERE movement_type = 'STOCK_OUT'", null);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (total == 0) {
            try {
                Cursor billItemsCursor = db.rawQuery("SELECT SUM(quantity) FROM " + TABLE_BILL_ITEMS, null);
                if (billItemsCursor != null && billItemsCursor.moveToFirst()) {
                    total = billItemsCursor.getInt(0);
                    billItemsCursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return total;
    }
}
