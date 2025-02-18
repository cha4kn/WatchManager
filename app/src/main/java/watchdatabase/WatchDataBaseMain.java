package watchdatabase;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import watchdatabase.dbhelpers.WatchDatabaseHelper;
import watchdatabase.gui.LoginManager;
import watchdatabase.gui.WatchManagerGUI;
import watchdatabase.models.LoginResult;

public class WatchDataBaseMain {
    
    private static final String WATCH_DB_URL = "jdbc:sqlite:app/src/main/resources/watches.db";
    private static final String USER_DB_URL = "jdbc:sqlite:app/src/main/resources/users.db";

    public static void main(String[] args) {
        setLookAndFeel();
        LoginManager loginManager = new LoginManager(USER_DB_URL);
        LoginResult loginResult = loginManager.getLoginResult();
        
        if (loginResult.success()) {
            String user = loginResult.user();
            WatchDatabaseHelper dbHelper = new WatchDatabaseHelper(WATCH_DB_URL);
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
}
