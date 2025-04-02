package dao;

import Utils.ConnexionDB;
import entite.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final Connection conn = ConnexionDB.getInstance();

    /**
     * Authenticate user by verifying the hashed password.
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

    public User getUserById(int id) {
        String sql = "SELECT id, username, role FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Create a new user with a hashed password.
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

    /**
     * Returns the user ID for a given username.
     */
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

    /**
     * NEW: Retrieves a User object by username.
     * Returns null if no such user exists.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, role FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a user by their id.
     */
    public boolean deleteUserById(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all users having the given role.
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users WHERE role = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUserPassword(int userId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;  // true if at least 1 row was updated
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
