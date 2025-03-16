package dao;

import entite.VehiculeDocument;
import entite.VehiculeDocument.DocType;
import Utils.ConnexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculeDocumentDao {

    private final Connection conn = ConnexionDB.getInstance();

    /**
     * Inserts a new VehiculeDocument record into DB.
     */
    public boolean create(VehiculeDocument doc) {
        String sql = "INSERT INTO vehicule_documents "
                + "(vehicule_id, doc_type, date_obtention, date_expiration, scanned_doc_url, cost, notified) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, doc.getVehiculeId());
            stmt.setString(2, doc.getDocType().name().toLowerCase());
            if (doc.getDateObtention() != null) {
                stmt.setDate(3, Date.valueOf(doc.getDateObtention()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            if (doc.getDateExpiration() != null) {
                stmt.setDate(4, Date.valueOf(doc.getDateExpiration()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setString(5, doc.getScannedDocUrl());
            stmt.setDouble(6, doc.getCost());
            stmt.setBoolean(7, false); // New documents are not notified
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doc.setDocId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves a VehiculeDocument by ID.
     */
    public Optional<VehiculeDocument> findById(int docId) {
        String sql = "SELECT * FROM vehicule_documents WHERE doc_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToVehiculeDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Retrieves all documents for a given vehicle.
     */
    public List<VehiculeDocument> findByVehiculeId(int vehiculeId) {
        List<VehiculeDocument> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule_documents WHERE vehicule_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToVehiculeDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all vehicule_documents.
     */
    public List<VehiculeDocument> findAll() {
        List<VehiculeDocument> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule_documents";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVehiculeDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates an existing VehiculeDocument record.
     */
    public boolean update(VehiculeDocument doc) {
        String sql = "UPDATE vehicule_documents "
                + "SET vehicule_id = ?, doc_type = ?, date_obtention = ?, "
                + "date_expiration = ?, scanned_doc_url = ?, cost = ?, notified = ? "
                + "WHERE doc_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doc.getVehiculeId());
            stmt.setString(2, doc.getDocType().name().toLowerCase());
            if (doc.getDateObtention() != null) {
                stmt.setDate(3, Date.valueOf(doc.getDateObtention()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            if (doc.getDateExpiration() != null) {
                stmt.setDate(4, Date.valueOf(doc.getDateExpiration()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setString(5, doc.getScannedDocUrl());
            stmt.setDouble(6, doc.getCost());
            stmt.setBoolean(7, doc.isNotified());
            stmt.setInt(8, doc.getDocId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a VehiculeDocument record by ID.
     */
    public boolean delete(int docId) {
        String sql = "DELETE FROM vehicule_documents WHERE doc_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a VehiculeDocument object (including cost and notified flag).
     */
    private VehiculeDocument mapResultSetToVehiculeDocument(ResultSet rs) throws SQLException {
        VehiculeDocument doc = new VehiculeDocument();
        doc.setDocId(rs.getInt("doc_id"));
        doc.setVehiculeId(rs.getInt("vehicule_id"));
        String docTypeStr = rs.getString("doc_type");
        VehiculeDocument.DocType docType = DocType.valueOf(docTypeStr.toUpperCase());
        doc.setDocType(docType);
        Date obtDate = rs.getDate("date_obtention");
        if (obtDate != null) {
            doc.setDateObtention(obtDate.toLocalDate());
        }
        Date expDate = rs.getDate("date_expiration");
        if (expDate != null) {
            doc.setDateExpiration(expDate.toLocalDate());
        }
        doc.setScannedDocUrl(rs.getString("scanned_doc_url"));
        doc.setCost(rs.getDouble("cost"));
        doc.setNotified(rs.getBoolean("notified"));
        return doc;
    }
}
