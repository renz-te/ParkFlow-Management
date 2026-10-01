package parkflowmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:parkflow_final.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Users table with security parameters
stmt.execute("CREATE TABLE IF NOT EXISTS users ("
        + "account_id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "username TEXT UNIQUE, "
        + "password TEXT, "
        + "role TEXT, "
        + "name TEXT, "
        + "age INTEGER, "
        + "contact TEXT, "
        + "status TEXT DEFAULT 'Active', "
        + "sec_q1 TEXT, "
        + "sec_a1 TEXT, "
        + "sec_q2 TEXT, "
        + "sec_a2 TEXT, "
        + "sec_q3 TEXT, "
        + "sec_a3 TEXT"
        + ")");

            // Default Admin Account
ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'admin'");
if (!rs.next()) {
    stmt.execute("INSERT INTO users (username, password, role, name, age, contact, status, sec_q1, sec_a1, sec_q2, sec_a2, sec_q3, sec_a3) "
            + "VALUES ('admin', 'admin123', 'Admin', 'System Administrator', 30, 'N/A', 'Active', 'Default Admin Key?', 'admin', 'N/A', 'N/A', 'N/A', 'N/A')");
}
rs.close();

// Default Staff Account
rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'staff'");
if (!rs.next()) {
    stmt.execute("INSERT INTO users (username, password, role, name, age, contact, status, sec_q1, sec_a1, sec_q2, sec_a2, sec_q3, sec_a3) "
            + "VALUES ('staff', 'staff123', 'Staff', 'Default Staff', 25, 'N/A', 'Active', 'Default Staff Key?', 'staff', 'N/A', 'N/A', 'N/A', 'N/A')");
}
rs.close();;

            // Parking Slots Table (Consolidated Schema)
            stmt.execute("CREATE TABLE IF NOT EXISTS parking_slots ("
                    + "slot_number INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "status TEXT, "
                    + "current_plate TEXT, "
                    + "current_category TEXT, "
                    + "entry_time TEXT, "
                    + "slot_type TEXT DEFAULT '4-Wheel')");

            // Populate 50 slots if empty, applying physical zone mapping automatically
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM parking_slots");
            if (rs.next() && rs.getInt("count") == 0) {
                for (int i = 1; i <= 50; i++) {
                    String type = (i <= 30) ? "4-Wheel" : "2-Wheel";
                    stmt.execute("INSERT INTO parking_slots (status, slot_type) VALUES ('Available', '" + type + "')");
                }
            }
            rs.close();
            // Create system_settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS system_settings ("
            + "setting_name TEXT PRIMARY KEY, "
            + "setting_value TEXT"
            + ")");

// Inject default parameters if table is empty
java.sql.ResultSet rsSettings = stmt.executeQuery("SELECT COUNT(*) AS count FROM system_settings");
if (rsSettings.next() && rsSettings.getInt("count") == 0) {
    stmt.execute("INSERT INTO system_settings (setting_name, setting_value) VALUES ('base_fare_2wheel', '30.00')");
    stmt.execute("INSERT INTO system_settings (setting_name, setting_value) VALUES ('hourly_rate_2wheel', '10.00')");
    stmt.execute("INSERT INTO system_settings (setting_name, setting_value) VALUES ('base_fare_4wheel', '50.00')");
    stmt.execute("INSERT INTO system_settings (setting_name, setting_value) VALUES ('hourly_rate_4wheel', '15.00')");
    stmt.execute("INSERT OR IGNORE INTO system_settings (setting_name, setting_value) VALUES ('grace_period', '15')");
    stmt.execute("INSERT OR IGNORE INTO system_settings (setting_name, setting_value) VALUES ('lost_ticket_penalty', '500.00')");
    stmt.execute("INSERT OR IGNORE INTO system_settings (setting_name, setting_value) VALUES ('overnight_surcharge', '300.00')");
    stmt.execute("INSERT OR IGNORE INTO system_settings (setting_name, setting_value) VALUES ('retention_days', '30')");
}
rsSettings.close();

        stmt.execute("INSERT OR IGNORE INTO system_settings (setting_name, setting_value) VALUES ('improper_parking_penalty', '200.00')"); 

            rs.close();

          stmt.execute("CREATE TABLE IF NOT EXISTS vehicle_history ("
        + "receipt_no INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "slot TEXT, "
        + "plate_number TEXT, "
        + "category TEXT, "
        + "entry_time TEXT, "
        + "exit_time TEXT, "
        + "fare REAL, "
        + "shift_id INTEGER"
        + ")");
            // Shift Reports Table
            stmt.execute("CREATE TABLE IF NOT EXISTS shift_reports (shift_id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER, login_time TEXT, logout_time TEXT, shift_revenue REAL, declared_cash REAL, discrepancy REAL)");

            // Create System Logs Table
stmt.execute("CREATE TABLE IF NOT EXISTS system_logs ("
    + "log_id INTEGER PRIMARY KEY AUTOINCREMENT, "
    + "event_type TEXT, "
    + "username TEXT, "
    + "details TEXT, "
    + "timestamp TEXT, "
    + "is_read INTEGER DEFAULT 0)");

// Append Status Reason Column to Users Table
try {
    stmt.execute("ALTER TABLE users ADD COLUMN status_reason TEXT DEFAULT 'N/A'");
} catch (java.sql.SQLException e) {
    // Column exists; ignore exception
}

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}