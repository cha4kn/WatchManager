package watchdatabase.dbhelpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import watchdatabase.models.Watch;

public class WatchDatabaseHelper {

    private final String URL; // Path to SQLite DB file

    public WatchDatabaseHelper (String dbURL) {
        this.URL = dbURL;
    }

    // Get database connection
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Insert a watch into the database
    public void insertWatch(String brand, String model, double price, String imagePath, String user) {
        String insertQuery = "INSERT INTO watches (brand, model, price, imagePath, user) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, brand);
            pstmt.setString(2, model);
            pstmt.setDouble(3, price);
            pstmt.setString(4, imagePath);
            pstmt.setString(5, user);
            pstmt.executeUpdate();
            System.out.println("Watch inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateWatch(int id, String brand, String model, double price, String imagePath, String user) {
        String query = "UPDATE watches SET brand = ?, model = ?, price = ?, imagePath = ?, user=? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, brand);
            pstmt.setString(2, model);
            pstmt.setDouble(3, price);
            pstmt.setString(4, imagePath);
            pstmt.setString(5, user);
            pstmt.setInt(6, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteWatchById(int id) {
        String query = "DELETE FROM watches WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);  // Set the ID parameter for the query
            int rowsAffected = pstmt.executeUpdate();  // Execute the DELETE query
            return rowsAffected > 0;  // Return true if a row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // Return false if there was an error or no row was deleted
    }

    public Watch getWatchById(int id) {
        String query = "SELECT * FROM watches WHERE id = ?";  // SQL query to select a watch by ID
        Watch watch = null;  // This will hold the watch if found

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);  // Set the ID parameter for the query

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Retrieve the data and create a Watch object
                    String brand = rs.getString("brand");
                    String model = rs.getString("model");
                    double price = rs.getDouble("price");
                    String imagePath = rs.getString("imagePath");  // Can be null if no image exists
                    String user = rs.getString("user");

                    // Create a Watch object with the retrieved data
                    watch = new Watch(id, brand, model, price, imagePath, user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return watch;  // Return the watch or null if not found
    }

    // Retrieve all watches from the database
    public List<Watch> getAllWatches() {
        List<Watch> watches = new ArrayList<>();
        String query = "SELECT * FROM watches";
        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                double price = rs.getDouble("price");
                String imagePath = rs.getString("imagePath");
                String user = rs.getString("user");
                watches.add(new Watch(id, brand, model, price, imagePath, user));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return watches;
    }

    public List<Watch> getAllWatchesForUser(String user) {
        List<Watch> watches = new ArrayList<>();
        String query = "SELECT * FROM watches WHERE user IS \'" + user + "\'";
        try (Connection conn = this.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                double price = rs.getDouble("price");
                String imagePath = rs.getString("imagePath");
                watches.add(new Watch(id, brand, model, price, imagePath, user));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return watches;
    }
}