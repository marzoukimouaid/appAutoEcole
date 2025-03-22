package dao;

import entite.ExamenCode;
import entite.ExamenCode.ExamStatus;
import entite.ExamenCode.PaymentStatus;
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

public class ExamenCodeDao {

    private final Connection conn = ConnexionDB.getInstance();

    public boolean create(ExamenCode exam) {
        String sql = "INSERT INTO examen_code "
                + "(candidat_id, moniteur_id, exam_datetime, status, price, paiement_status, payment_date, notified, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, exam.getCandidatId());
            stmt.setInt(2, exam.getMoniteurId());
            stmt.setTimestamp(3, Timestamp.valueOf(exam.getExamDatetime()));
            stmt.setString(4, exam.getStatus().name());
            stmt.setDouble(5, exam.getPrice());
            stmt.setString(6, exam.getPaiementStatus().name());

            if (exam.getPaymentDate() != null) {
                stmt.setDate(7, Date.valueOf(exam.getPaymentDate()));
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.setBoolean(8, exam.isNotified());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        exam.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<ExamenCode> findById(int id) {
        String sql = "SELECT * FROM examen_code WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<ExamenCode> findByCandidatId(int candidatId) {
        List<ExamenCode> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_code WHERE candidat_id = ? ORDER BY exam_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, candidatId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public List<ExamenCode> findByMoniteurId(int moniteurId) {
        List<ExamenCode> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_code WHERE moniteur_id = ? ORDER BY exam_datetime ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, moniteurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public boolean update(ExamenCode exam) {
        String sql = "UPDATE examen_code "
                + "SET candidat_id = ?, moniteur_id = ?, exam_datetime = ?, status = ?, "
                + "    price = ?, paiement_status = ?, payment_date = ?, notified = ?, updated_at = NOW() "
                + "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exam.getCandidatId());
            stmt.setInt(2, exam.getMoniteurId());
            stmt.setTimestamp(3, Timestamp.valueOf(exam.getExamDatetime()));
            stmt.setString(4, exam.getStatus().name());
            stmt.setDouble(5, exam.getPrice());
            stmt.setString(6, exam.getPaiementStatus().name());

            if (exam.getPaymentDate() != null) {
                stmt.setDate(7, Date.valueOf(exam.getPaymentDate()));
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.setBoolean(8, exam.isNotified());
            stmt.setInt(9, exam.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM examen_code WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ExamenCode> getAllExams() {
        List<ExamenCode> exams = new ArrayList<>();
        String sql = "SELECT * FROM examen_code ORDER BY exam_datetime DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                exams.add(mapResultSetToExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    private ExamenCode mapResultSetToExam(ResultSet rs) throws SQLException {
        ExamenCode exam = new ExamenCode();
        exam.setId(rs.getInt("id"));
        exam.setCandidatId(rs.getInt("candidat_id"));
        exam.setMoniteurId(rs.getInt("moniteur_id"));
        exam.setExamDatetime(rs.getTimestamp("exam_datetime").toLocalDateTime());
        exam.setStatus(ExamStatus.valueOf(rs.getString("status")));
        exam.setPrice(rs.getDouble("price"));
        exam.setPaiementStatus(PaymentStatus.valueOf(rs.getString("paiement_status")));

        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            exam.setPaymentDate(paymentDate.toLocalDate());
        }

        exam.setNotified(rs.getBoolean("notified"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            exam.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            exam.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        return exam;
    }
}
