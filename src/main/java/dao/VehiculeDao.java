package dao;

import entite.Vehicule;
import entite.Vehicule.VehicleType;
import Utils.ConnexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculeDao {

    private static final Connection conn = ConnexionDB.getInstance();

    public boolean immatriculationExists(String immatriculation) {
        String sql = "SELECT COUNT(*) FROM vehicules WHERE immatriculation = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, immatriculation);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createVehicule(Vehicule vehicule) {
        if (immatriculationExists(vehicule.getImmatriculation())) {
            System.err.println("Error: A vehicle with this immatriculation already exists.");
            return false;
        }
        String sql = "INSERT INTO vehicules (immatriculation, marque, date_mise_en_service, kilometrage_total, km_restant_entretien, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setDate(3, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(4, vehicule.getKilometrageTotal());
            stmt.setInt(5, vehicule.getKmRestantEntretien());
            stmt.setString(6, vehicule.getType().name());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vehicule.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public Optional<Vehicule> getVehiculeById(int id) {
        String sql = "SELECT * FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id"));
                    vehicule.setImmatriculation(rs.getString("immatriculation"));
                    vehicule.setMarque(rs.getString("marque"));
                    vehicule.setDateMiseEnService(rs.getDate("date_mise_en_service").toLocalDate());
                    vehicule.setKilometrageTotal(rs.getInt("kilometrage_total"));
                    vehicule.setKmRestantEntretien(rs.getInt("km_restant_entretien"));
                    vehicule.setType(VehicleType.valueOf(rs.getString("type")));
                    return Optional.of(vehicule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Vehicule> getAllVehicules() {
        List<Vehicule> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicules ORDER BY id DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vehicule vehicule = new Vehicule(
                        rs.getInt("id"),
                        rs.getString("immatriculation"),
                        rs.getString("marque"),
                        rs.getDate("date_mise_en_service").toLocalDate(),
                        rs.getInt("kilometrage_total"),
                        rs.getInt("km_restant_entretien"),
                        VehicleType.valueOf(rs.getString("type"))
                );
                list.add(vehicule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateVehicule(Vehicule vehicule) {
        String sql = "UPDATE vehicules SET immatriculation=?, marque=?, date_mise_en_service=?, kilometrage_total=?, km_restant_entretien=?, type=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setDate(3, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(4, vehicule.getKilometrageTotal());
            stmt.setInt(5, vehicule.getKmRestantEntretien());
            stmt.setString(6, vehicule.getType().name());
            stmt.setInt(7, vehicule.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteVehicule(int id) {
        String sql = "DELETE FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
