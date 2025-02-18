package watchdatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import watchdatabase.dbhelpers.WatchDatabaseHelper;
import watchdatabase.gui.LoginManager;
import watchdatabase.gui.WatchManagerGUI;
import watchdatabase.models.LoginResult;

public class WatchDataBaseMain {
    
    private static final String DB_URL = "jdbc:sqlite:app/src/main/resources/watchmanager.db";

    public static void main(String[] args) {
        initDatabase();
        setLookAndFeel();
        
        // Log in user
        LoginManager loginManager = new LoginManager(DB_URL);
        LoginResult loginResult = loginManager.getLoginResult();
        
        if (loginResult.success()) {
            String user = loginResult.user();
            WatchDatabaseHelper dbHelper = new WatchDatabaseHelper(DB_URL);
            WatchManagerGUI frontend = new WatchManagerGUI(dbHelper, user);
            frontend.run();
        } else {
            System.out.println("Error when logging in...");
            System.exit(1);
        }
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initDatabase() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                   "username TEXT UNIQUE NOT NULL, " +
                                   "password TEXT NOT NULL)";

        String createWatchTable = "CREATE TABLE IF NOT EXISTS watches (" +
                                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                   "brand TEXT NOT NULL, " +
                                   "model TEXT NOT NULL, " +
                                   "price REAL NOT NULL, " +
                                   "imagePath TEXT, " +
                                   "user TEXT NOT NULL, " +
                                   "FOREIGN KEY(user) REFERENCES users(username))";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create both tables in the same database
            stmt.execute(createUserTable);
            stmt.execute(createWatchTable);

            System.out.println("Tables created successfully in watchmanager.db!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean loginUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
