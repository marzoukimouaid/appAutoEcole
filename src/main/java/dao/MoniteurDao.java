package dao;

import Utils.ConnexionDB;
import entite.Moniteur;
import entite.Moniteur.PermisType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoniteurDao {

    private static final Connection conn = ConnexionDB.getInstance();

    /**
     * Inserts a new moniteur into the database.
     */
    public boolean createMoniteur(Moniteur moniteur) {
        String sql = "INSERT INTO moniteurs (user_id, permis_type) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteur.getUserId());
            stmt.setString(2, moniteur.getPermisType().name());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a moniteur by user id.
     */
    public Optional<Moniteur> getMoniteurByUserId(int userId) {
        String sql = "SELECT user_id, permis_type FROM moniteurs WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int uid = rs.getInt("user_id");
                    String permisStr = rs.getString("permis_type");
                    PermisType permis = PermisType.valueOf(permisStr);
                    return Optional.of(new Moniteur(uid, permis));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Updates an existing moniteur's permis type.
     */
    public boolean updateMoniteur(Moniteur moniteur) {
        String sql = "UPDATE moniteurs SET permis_type = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, moniteur.getPermisType().name());
            stmt.setInt(2, moniteur.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all moniteurs from the database.
     */
    public List<Moniteur> getAllMoniteurs() {
        List<Moniteur> list = new ArrayList<>();
        String sql = "SELECT * FROM moniteurs ORDER BY user_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int uid = rs.getInt("user_id");
                String permisStr = rs.getString("permis_type");
                PermisType permis = PermisType.valueOf(permisStr);
                Moniteur moniteur = new Moniteur(uid, permis);
                list.add(moniteur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Deletes the moniteur record for the given userId.
     * (Part of "cascade" deletion logic.)
     */
    public boolean deleteMoniteurByUserId(int userId) {
        String sql = "DELETE FROM moniteurs WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
