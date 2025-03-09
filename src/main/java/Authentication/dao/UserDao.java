package Authentication.dao;

import Utils.ConnexionDB;
import Authentication.entite.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    private static final Connection conn = ConnexionDB.getInstance();

    /**
     * Authenticate user by verifying the hashed password.
     *
     * @param username The username provided by the user.
     * @param password The raw password entered by the user.
     * @return A User object if authentication is successful, otherwise null.
     */
    public User getUserByUsernameAndPassword(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Authentication failed
    }

    /**
     * Create a new user with a hashed password.
     *
     * @param username The username to create.
     * @param password The raw password to be hashed and stored.
     * @param role     The role of the user (secretaire, candidat, ingenieur).
     * @return True if the user was created successfully, otherwise false.
     */
    public boolean createUser(String username, String password, String role) {
        if (userExists(username)) {
            return false; // Prevent duplicate usernames
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a user already exists in the database.
     *
     * @param username The username to check.
     * @return True if the user exists, otherwise false.
     */
    private boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if user not found
    }
}
