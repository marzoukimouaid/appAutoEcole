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


    public boolean createMoniteur(Moniteur moniteur) {
        String sql = "INSERT INTO moniteurs (user_id, permis_type, salaire) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteur.getUserId());
            stmt.setString(2, moniteur.getPermisType().name());
            stmt.setDouble(3, moniteur.getSalaire());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Optional<Moniteur> getMoniteurByUserId(int userId) {
        String sql = "SELECT user_id, permis_type, salaire FROM moniteurs WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Moniteur moniteur = new Moniteur();
                    moniteur.setUserId(rs.getInt("user_id"));
                    moniteur.setPermisType(PermisType.valueOf(rs.getString("permis_type")));
                    moniteur.setSalaire(rs.getDouble("salaire"));
                    return Optional.of(moniteur);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public boolean updateMoniteur(Moniteur moniteur) {
        String sql = "UPDATE moniteurs SET permis_type = ?, salaire = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, moniteur.getPermisType().name());
            stmt.setDouble(2, moniteur.getSalaire());
            stmt.setInt(3, moniteur.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Moniteur> getAllMoniteurs() {
        List<Moniteur> list = new ArrayList<>();
        String sql = "SELECT * FROM moniteurs ORDER BY user_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Moniteur moniteur = new Moniteur();
                moniteur.setUserId(rs.getInt("user_id"));
                moniteur.setPermisType(PermisType.valueOf(rs.getString("permis_type")));
                moniteur.setSalaire(rs.getDouble("salaire"));
                list.add(moniteur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


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
