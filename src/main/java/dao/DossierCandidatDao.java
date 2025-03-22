package dao;

import entite.DossierCandidat;
import Utils.ConnexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DossierCandidatDao {

    private static final Connection conn = ConnexionDB.getInstance();

    /**
     * Inserts a new dossier candidat into the database.
     */
    public boolean createDossierCandidat(DossierCandidat dossier) {
        String sql = "INSERT INTO dossier_candidats (cin_url, certificat_medical_url, photo_identite_url, created_at, updated_at, candidate_id, permis_type, nombre_seances_conduite, nombre_seances_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dossier.getCinUrl());
            stmt.setString(2, dossier.getCertificatMedicalUrl());
            stmt.setString(3, dossier.getPhotoIdentiteUrl());
            stmt.setTimestamp(4, Timestamp.valueOf(dossier.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(dossier.getUpdatedAt()));
            stmt.setInt(6, dossier.getCandidateId());
            stmt.setString(7, dossier.getPermisType());
            stmt.setInt(8, dossier.getNombreSeancesConduite());
            stmt.setInt(9, dossier.getNombreSeancesCode());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a dossier candidat by candidate id.
     */
    public Optional<DossierCandidat> getDossierCandidatByCandidateId(int candidateId) {
        String sql = "SELECT * FROM dossier_candidats WHERE candidate_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DossierCandidat dossier = mapResultSetToDossier(rs);
                    return Optional.of(dossier);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retrieves all dossier candidats from the database, ordered by creation date (newest first).
     */
    public List<DossierCandidat> getAllDossierCandidats() {
        List<DossierCandidat> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier_candidats ORDER BY created_at DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dossiers.add(mapResultSetToDossier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dossiers;
    }

    /**
     * Deletes the dossier candidat for the given candidate id.
     */
    public boolean deleteDossierCandidate(int candidateId) {
        String sql = "DELETE FROM dossier_candidats WHERE candidate_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidateId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private DossierCandidat mapResultSetToDossier(ResultSet rs) throws SQLException {
        DossierCandidat dossier = new DossierCandidat();
        dossier.setId(rs.getInt("id"));
        dossier.setCinUrl(rs.getString("cin_url"));
        dossier.setCertificatMedicalUrl(rs.getString("certificat_medical_url"));
        dossier.setPhotoIdentiteUrl(rs.getString("photo_identite_url"));
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            dossier.setCreatedAt(createdTimestamp.toLocalDateTime());
        }
        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            dossier.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }
        dossier.setCandidateId(rs.getInt("candidate_id"));
        dossier.setPermisType(rs.getString("permis_type"));
        dossier.setNombreSeancesConduite(rs.getInt("nombre_seances_conduite"));
        dossier.setNombreSeancesCode(rs.getInt("nombre_seances_code"));
        return dossier;
    }

    /**
     * Updates an existing dossier candidat in the database.
     */
    public boolean updateDossierCandidat(DossierCandidat dossier) {
        String sql = "UPDATE dossier_candidats SET cin_url = ?, certificat_medical_url = ?, photo_identite_url = ?, updated_at = ?, permis_type = ?, nombre_seances_conduite = ?, nombre_seances_code = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dossier.getCinUrl());
            stmt.setString(2, dossier.getCertificatMedicalUrl());
            stmt.setString(3, dossier.getPhotoIdentiteUrl());
            stmt.setTimestamp(4, Timestamp.valueOf(dossier.getUpdatedAt()));
            stmt.setString(5, dossier.getPermisType());
            stmt.setInt(6, dossier.getNombreSeancesConduite());
            stmt.setInt(7, dossier.getNombreSeancesCode());
            stmt.setInt(8, dossier.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
