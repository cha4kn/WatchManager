package watchdatabase.dbhelpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDatabaseHelper {
    private static final Logger logger = LoggerFactory.getLogger(UserDatabaseHelper.class);

    private String dbUrl;

    public UserDatabaseHelper(String dbUrl) {
        this.dbUrl = dbUrl;
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
            logger.error("Caught SQLException when adding user: {}", e.toString());
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
                logger.debug("Successfully deleted user: {}", username);
                return true;
            } else {
                throw new Exception("User not found.");
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}