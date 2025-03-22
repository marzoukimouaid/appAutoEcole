package dao;

import entite.ExamenConduit;
import entite.ExamenConduit.ExamStatus;
import entite.ExamenConduit.PaymentStatus;
import Utils.ConnexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamenConduitDao {

    private final Connection conn = ConnexionDB.getInstance();

    public boolean create(ExamenConduit exam) {
        String sql = "INSERT INTO examen_conduit "
                + "(candidat_id, moniteur_id, vehicule_id, exam_datetime, latitude, longitude, status, price, paiement_status, payment_date, notified, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, exam.getCandidatId());
            stmt.setInt(2, exam.getMoniteurId());
            stmt.setInt(3, exam.getVehiculeId());
            stmt.setTimestamp(4, Timestamp.valueOf(exam.getExamDatetime()));
            stmt.setDouble(5, exam.getLatitude());
            stmt.setDouble(6, exam.getLongitude());
            stmt.setString(7, exam.getStatus().name());
            stmt.setDouble(8, exam.getPrice());
            stmt.setString(9, exam.getPaiementStatus().name());

            if (exam.getPaymentDate() != null) {
                stmt.setDate(10, Date.valueOf(exam.getPaymentDate()));
            } else {
                stmt.setNull(10, Types.DATE);
            }

            stmt.setBoolean(11, exam.isNotified());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exam.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<ExamenConduit> findById(int id) {
        String sql = "SELECT * FROM examen_conduit WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return Optional.of(mapResultSetToExam(rs));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<ExamenConduit> findByCandidatId(int candidatId) {
        List<ExamenConduit> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_conduit WHERE candidat_id = ? ORDER BY exam_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidatId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public List<ExamenConduit> findByMoniteurId(int moniteurId) {
        List<ExamenConduit> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_conduit WHERE moniteur_id = ? ORDER BY exam_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteurId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public boolean update(ExamenConduit exam) {
        String sql = "UPDATE examen_conduit "
                + "SET candidat_id = ?, moniteur_id = ?, vehicule_id = ?, exam_datetime = ?, "
                + "    latitude = ?, longitude = ?, status = ?, price = ?, paiement_status = ?, payment_date = ?, notified = ?, updated_at = NOW() "
                + "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exam.getCandidatId());
            stmt.setInt(2, exam.getMoniteurId());
            stmt.setInt(3, exam.getVehiculeId());
            stmt.setTimestamp(4, Timestamp.valueOf(exam.getExamDatetime()));
            stmt.setDouble(5, exam.getLatitude());
            stmt.setDouble(6, exam.getLongitude());
            stmt.setString(7, exam.getStatus().name());
            stmt.setDouble(8, exam.getPrice());
            stmt.setString(9, exam.getPaiementStatus().name());

            if (exam.getPaymentDate() != null) {
                stmt.setDate(10, Date.valueOf(exam.getPaymentDate()));
            } else {
                stmt.setNull(10, Types.DATE);
            }

            stmt.setBoolean(11, exam.isNotified());
            stmt.setInt(12, exam.getId());

            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM examen_conduit WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ExamenConduit> getAllExams() {
        List<ExamenConduit> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_conduit ORDER BY exam_datetime DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    private ExamenConduit mapResultSetToExam(ResultSet rs) throws SQLException {
        ExamenConduit exam = new ExamenConduit();
        exam.setId(rs.getInt("id"));
        exam.setCandidatId(rs.getInt("candidat_id"));
        exam.setMoniteurId(rs.getInt("moniteur_id"));
        exam.setVehiculeId(rs.getInt("vehicule_id"));
        exam.setExamDatetime(rs.getTimestamp("exam_datetime").toLocalDateTime());
        exam.setLatitude(rs.getDouble("latitude"));
        exam.setLongitude(rs.getDouble("longitude"));
        exam.setStatus(ExamStatus.valueOf(rs.getString("status")));
        exam.setPrice(rs.getDouble("price"));
        exam.setPaiementStatus(PaymentStatus.valueOf(rs.getString("paiement_status")));

        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            exam.setPaymentDate(paymentDate.toLocalDate());
        }

        exam.setNotified(rs.getBoolean("notified"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if(createdTs != null) {
            exam.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if(updatedTs != null) {
            exam.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return exam;
    }
}
