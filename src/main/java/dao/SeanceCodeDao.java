package dao;

import entite.SeanceCode;
import Utils.ConnexionDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeanceCodeDao {

    private final Connection conn = ConnexionDB.getInstance();

    public boolean create(SeanceCode seance) {
        String sql = "INSERT INTO seance_code (candidat_id, moniteur_id, session_datetime, created_at, updated_at) " +
                "VALUES (?, ?, ?, NOW(), NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, seance.getCandidatId());
            stmt.setInt(2, seance.getMoniteurId());
            stmt.setTimestamp(3, Timestamp.valueOf(seance.getSessionDatetime()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        seance.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<SeanceCode> findById(int id) {
        String sql = "SELECT * FROM seance_code WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToSeanceCode(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<SeanceCode> findByCandidatId(int candidatId) {
        List<SeanceCode> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_code WHERE candidat_id = ? ORDER BY session_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidatId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeanceCode(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    // NEW: Retrieve seances by moniteur id.
    public List<SeanceCode> findByMoniteurId(int moniteurId) {
        List<SeanceCode> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_code WHERE moniteur_id = ? ORDER BY session_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeanceCode(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public boolean update(SeanceCode seance) {
        String sql = "UPDATE seance_code SET candidat_id = ?, moniteur_id = ?, session_datetime = ?, updated_at = NOW() " +
                "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seance.getCandidatId());
            stmt.setInt(2, seance.getMoniteurId());
            stmt.setTimestamp(3, Timestamp.valueOf(seance.getSessionDatetime()));
            stmt.setInt(4, seance.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM seance_code WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private SeanceCode mapResultSetToSeanceCode(ResultSet rs) throws SQLException {
        SeanceCode seance = new SeanceCode();
        seance.setId(rs.getInt("id"));
        seance.setCandidatId(rs.getInt("candidat_id"));
        seance.setMoniteurId(rs.getInt("moniteur_id"));
        seance.setSessionDatetime(rs.getTimestamp("session_datetime").toLocalDateTime());
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            seance.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            seance.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return seance;
    }

    public List<SeanceCode> getAllSeances() {
        List<SeanceCode> seances = new ArrayList<>();
        String sql = "SELECT * FROM seance_code ORDER BY session_datetime DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                seances.add(mapResultSetToSeanceCode(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

}
