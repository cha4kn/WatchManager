package watchdatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatDarkLaf;

import watchdatabase.dbhelpers.WatchDatabaseHelper;
import watchdatabase.gui.LoginManager;
import watchdatabase.gui.WatchManagerGUI;
import watchdatabase.models.LoginResult;

public class WatchDataBaseMain {
    private static final Logger logger = LoggerFactory.getLogger(WatchDataBaseMain.class);

    private static final String path = WatchDataBaseMain.class.getResource("/watchmanager.db").getPath();
    public static final String DB_URL = "jdbc:sqlite:" + path;

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
            logger.warn("Login cancelled. Exiting...");
            System.exit(1);
        }
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            logger.error("Caught exception when setting look and feel: " + e.toString());
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

            logger.debug("Tables created successfully in watchmanager.db!");

        } catch (SQLException e) {
            logger.error("Caught SQLException when creating tables: " + e.toString());
        }
    }
}
