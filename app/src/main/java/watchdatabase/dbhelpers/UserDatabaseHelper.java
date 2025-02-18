package watchdatabase.dbhelpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

public class UserDatabaseHelper {

    private String dbUrl;

    public UserDatabaseHelper(String dbUrl) {
/*         URL resourceUrl = UserDatabaseHelper.class.getClassLoader().getResource("resources/users.db");

        if (resourceUrl == null) {
            // If the resource is not found via the class loader, fall back to a default path
            Path currentDir = Paths.get("").toAbsolutePath();
            Path dbPath = currentDir.resolve("resources/users.db");
            DB_URL = "jdbc:sqlite:" + dbPath.toString();
        } else {
            try {
                URI uri = resourceUrl.toURI();
                File file = new File(uri);
                DB_URL = "jdbc:sqlite:" + file.getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } */
       
        this.dbUrl = dbUrl;
        try {
            createTable();
        } catch (SQLException e) {
            System.out.println("Caught exception when creating table: " + e.toString());
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username TEXT UNIQUE NOT NULL," +
                     "password TEXT NOT NULL)";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw e;
        }
    }

    public boolean addUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        
        String passwordHashed = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHashed);
            pstmt.executeUpdate();
            System.out.println("User added successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the hashed password for a user
     * @param username
     * @return
     * @throws SQLException
     */
    private Optional<String> getUserPassword(String username) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(rs.getString("password"));
            }
        } catch (SQLException e) {
            throw e;
        }
        return Optional.empty();
    }

    /**
     * Takes the hashed password and compares it with the stored one.
     * @param user
     * @param suppliedPassword
     * @return
     * @throws Exception 
     */
    public boolean verifyUserPassword(String user, String suppliedPassword) throws Exception {
        return BCrypt.checkpw(suppliedPassword, getUserPassword(user).get());
    }

    public boolean updateUserPassword(String username, String newPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        String passwordHashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        try (Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHashed);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Password updated successfully.");
                return true;
            } else {
                throw new Exception("User not found.");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public boolean deleteUser(String username) throws Exception {
        String sql = "DELETE FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
                return true;
            } else {
                throw new Exception("User not found.");
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}