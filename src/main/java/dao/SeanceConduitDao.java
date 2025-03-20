package dao;

import entite.SeanceConduit;
import Utils.ConnexionDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceConduitDao {

    private final Connection conn = ConnexionDB.getInstance();

    public boolean create(SeanceConduit seance) {
        String sql = "INSERT INTO seance_conduit (candidat_id, moniteur_id, vehicule_id, session_datetime, latitude, longitude, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, seance.getCandidatId());
            stmt.setInt(2, seance.getMoniteurId());
            stmt.setInt(3, seance.getVehiculeId());
            stmt.setTimestamp(4, Timestamp.valueOf(seance.getSessionDatetime()));
            stmt.setDouble(5, seance.getLatitude());
            stmt.setDouble(6, seance.getLongitude());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        seance.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<SeanceConduit> findById(int id) {
        String sql = "SELECT * FROM seance_conduit WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return Optional.of(mapResultSetToSeanceConduit(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<SeanceConduit> findByCandidatId(int candidatId) {
        List<SeanceConduit> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_conduit WHERE candidat_id = ? ORDER BY session_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidatId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                seances.add(mapResultSetToSeanceConduit(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public List<SeanceConduit> findByMoniteurId(int moniteurId) {
        List<SeanceConduit> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_conduit WHERE moniteur_id = ? ORDER BY session_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteurId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                seances.add(mapResultSetToSeanceConduit(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public boolean update(SeanceConduit seance) {
        String sql = "UPDATE seance_conduit SET candidat_id = ?, moniteur_id = ?, vehicule_id = ?, session_datetime = ?, latitude = ?, longitude = ?, updated_at = NOW() " +
                "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seance.getCandidatId());
            stmt.setInt(2, seance.getMoniteurId());
            stmt.setInt(3, seance.getVehiculeId());
            stmt.setTimestamp(4, Timestamp.valueOf(seance.getSessionDatetime()));
            stmt.setDouble(5, seance.getLatitude());
            stmt.setDouble(6, seance.getLongitude());
            stmt.setInt(7, seance.getId());
            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM seance_conduit WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<SeanceConduit> getAllSeances() {
        List<SeanceConduit> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_conduit ORDER BY session_datetime DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                seances.add(mapResultSetToSeanceConduit(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }


    private SeanceConduit mapResultSetToSeanceConduit(ResultSet rs) throws SQLException {
        SeanceConduit seance = new SeanceConduit();
        seance.setId(rs.getInt("id"));
        seance.setCandidatId(rs.getInt("candidat_id"));
        seance.setMoniteurId(rs.getInt("moniteur_id"));
        seance.setVehiculeId(rs.getInt("vehicule_id"));
        seance.setSessionDatetime(rs.getTimestamp("session_datetime").toLocalDateTime());
        seance.setLatitude(rs.getDouble("latitude"));
        seance.setLongitude(rs.getDouble("longitude"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if(createdTs != null) {
            seance.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if(updatedTs != null) {
            seance.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return seance;
    }
}
