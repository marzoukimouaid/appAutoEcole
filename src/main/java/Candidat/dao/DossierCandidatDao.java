package Candidat.dao;

import Candidat.entite.DossierCandidat;
import Utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class DossierCandidatDao {

    private static final Connection conn = ConnexionDB.getInstance();

    /**
     * Inserts a new dossier candidat into the database.
     *
     * @param dossier The dossier candidat to create.
     * @return true if creation was successful; false otherwise.
     */
    public boolean createDossierCandidat(DossierCandidat dossier) {
        String sql = "INSERT INTO dossier_candidats (cin_url, certificat_medical_url, photo_identite_url, created_at, updated_at, candidate_id, permis_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dossier.getCinUrl());
            stmt.setString(2, dossier.getCertificatMedicalUrl());
            stmt.setString(3, dossier.getPhotoIdentiteUrl());
            stmt.setTimestamp(4, Timestamp.valueOf(dossier.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(dossier.getUpdatedAt()));
            stmt.setInt(6, dossier.getCandidateId());
            stmt.setString(7, dossier.getPermisType());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a dossier candidat by candidate id.
     *
     * @param candidateId The id of the candidate.
     * @return An Optional containing the dossier if found, or empty otherwise.
     */
    public Optional<DossierCandidat> getDossierCandidatByCandidateId(int candidateId) {
        String sql = "SELECT * FROM dossier_candidats WHERE candidate_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DossierCandidat dossier = new DossierCandidat();
                    dossier.setId(rs.getInt("id"));
                    dossier.setCinUrl(rs.getString("cin_url"));
                    dossier.setCertificatMedicalUrl(rs.getString("certificat_medical_url"));
                    dossier.setPhotoIdentiteUrl(rs.getString("photo_identite_url"));
                    dossier.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    dossier.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    dossier.setCandidateId(rs.getInt("candidate_id"));
                    dossier.setPermisType(rs.getString("permis_type"));
                    return Optional.of(dossier);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Additional methods for update, delete, etc., can be added following similar patterns.
}
