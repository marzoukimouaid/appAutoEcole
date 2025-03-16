package dao;

import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import Utils.ConnexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentInstallmentDao {

    private static final Connection conn = ConnexionDB.getInstance();

    // Inserts a new installment record.
    public boolean create(PaymentInstallment installment) {
        String sql = "INSERT INTO paiements_installments " +
                "(paiement_id, installment_number, due_date, amount_due, status, date_paid, notified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, installment.getPaymentId());
            stmt.setInt(2, installment.getInstallmentNumber());
            stmt.setDate(3, Date.valueOf(installment.getDueDate()));
            stmt.setDouble(4, installment.getAmountDue());
            stmt.setString(5, installment.getStatus().name());
            if (installment.getDatePaid() != null) {
                stmt.setDate(6, Date.valueOf(installment.getDatePaid()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            stmt.setBoolean(7, installment.isNotified());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        installment.setInstallmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Retrieves an installment by its id.
    public Optional<PaymentInstallment> findById(int installmentId) {
        String sql = "SELECT * FROM paiements_installments WHERE installment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, installmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInstallment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Retrieves all installments for a given payment.
    public List<PaymentInstallment> findByPaymentId(int paymentId) {
        List<PaymentInstallment> installments = new ArrayList<>();
        String sql = "SELECT * FROM paiements_installments WHERE paiement_id = ? ORDER BY installment_number ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    installments.add(mapResultSetToInstallment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return installments;
    }

    // Updates an installment record.
    public boolean update(PaymentInstallment installment) {
        String sql = "UPDATE paiements_installments SET status = ?, date_paid = ?, notified = ? WHERE installment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, installment.getStatus().name());
            if (installment.getDatePaid() != null) {
                stmt.setDate(2, Date.valueOf(installment.getDatePaid()));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            stmt.setBoolean(3, installment.isNotified());
            stmt.setInt(4, installment.getInstallmentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PaymentInstallment mapResultSetToInstallment(ResultSet rs) throws SQLException {
        PaymentInstallment installment = new PaymentInstallment();
        installment.setInstallmentId(rs.getInt("installment_id"));
        installment.setPaymentId(rs.getInt("paiement_id"));
        installment.setInstallmentNumber(rs.getInt("installment_number"));
        installment.setDueDate(rs.getDate("due_date").toLocalDate());
        installment.setAmountDue(rs.getDouble("amount_due"));
        installment.setStatus(Status.valueOf(rs.getString("status")));
        Date datePaid = rs.getDate("date_paid");
        if (datePaid != null) {
            installment.setDatePaid(datePaid.toLocalDate());
        }
        installment.setNotified(rs.getBoolean("notified"));
        return installment;
    }
}
