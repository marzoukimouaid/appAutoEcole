package dao;

import entite.Payment;
import Utils.ConnexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDao {

    private static final Connection conn = ConnexionDB.getInstance();

    public boolean create(Payment payment) {
        String sql = "INSERT INTO paiements (user_id, total_amount, payment_type, payment_date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getUserId());
            stmt.setDouble(2, payment.getTotalAmount());
            stmt.setString(3, payment.getPaymentType());
            stmt.setDate(4, Date.valueOf(payment.getPaymentDate()));
            stmt.setString(5, payment.getStatus());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<Payment> findById(int id) {
        String sql = "SELECT * FROM paiements WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Payment payment = mapResultSetToPayment(rs);
                    return Optional.of(payment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Payment> findByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM paiements WHERE user_id = ? ORDER BY payment_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    public boolean update(Payment payment) {
        String sql = "UPDATE paiements SET total_amount = ?, payment_type = ?, payment_date = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, payment.getTotalAmount());
            stmt.setString(2, payment.getPaymentType());
            stmt.setDate(3, Date.valueOf(payment.getPaymentDate()));
            stmt.setString(4, payment.getStatus());
            stmt.setInt(5, payment.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM paiements WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setTotalAmount(rs.getDouble("total_amount"));
        payment.setPaymentType(rs.getString("payment_type"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setStatus(rs.getString("status"));
        return payment;
    }
}
