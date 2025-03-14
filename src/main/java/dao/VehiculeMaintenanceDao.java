package dao;

import entite.VehiculeMaintenance;
import Utils.ConnexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for vehicule_maintenance table.
 */
public class VehiculeMaintenanceDao {

    private final Connection conn = ConnexionDB.getInstance();

    public boolean create(VehiculeMaintenance m) {
        String sql = "INSERT INTO vehicule_maintenance "
                + "(vehicule_id, date_maintenance, type_maintenance, description, cost, scanned_invoice_url) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, m.getVehiculeId());
            stmt.setDate(2, Date.valueOf(m.getDateMaintenance()));
            stmt.setString(3, m.getTypeMaintenance());
            stmt.setString(4, m.getDescription());
            stmt.setDouble(5, m.getCost());
            stmt.setString(6, m.getScannedInvoiceUrl());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<VehiculeMaintenance> findById(int maintenanceId) {
        String sql = "SELECT * FROM vehicule_maintenance WHERE maintenance_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maintenanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehiculeMaintenance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<VehiculeMaintenance> findByVehiculeId(int vehiculeId) {
        List<VehiculeMaintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule_maintenance WHERE vehicule_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehiculeMaintenance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<VehiculeMaintenance> findAll() {
        List<VehiculeMaintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicule_maintenance";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVehiculeMaintenance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean update(VehiculeMaintenance m) {
        String sql = "UPDATE vehicule_maintenance "
                + "SET vehicule_id = ?, date_maintenance = ?, type_maintenance = ?, "
                + "description = ?, cost = ?, scanned_invoice_url = ? "
                + "WHERE maintenance_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, m.getVehiculeId());
            stmt.setDate(2, Date.valueOf(m.getDateMaintenance()));
            stmt.setString(3, m.getTypeMaintenance());
            stmt.setString(4, m.getDescription());
            stmt.setDouble(5, m.getCost());
            stmt.setString(6, m.getScannedInvoiceUrl());
            stmt.setInt(7, m.getMaintenanceId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maintenanceId) {
        String sql = "DELETE FROM vehicule_maintenance WHERE maintenance_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maintenanceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private VehiculeMaintenance mapResultSetToVehiculeMaintenance(ResultSet rs) throws SQLException {
        VehiculeMaintenance m = new VehiculeMaintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        m.setVehiculeId(rs.getInt("vehicule_id"));

        Date dateMaint = rs.getDate("date_maintenance");
        if (dateMaint != null) {
            m.setDateMaintenance(dateMaint.toLocalDate());
        }
        m.setTypeMaintenance(rs.getString("type_maintenance"));
        m.setDescription(rs.getString("description"));
        m.setCost(rs.getDouble("cost"));
        m.setScannedInvoiceUrl(rs.getString("scanned_invoice_url"));
        return m;
    }
}
