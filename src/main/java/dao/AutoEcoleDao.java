package dao;

import Utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoEcoleDao {

    private static final Connection conn = ConnexionDB.getInstance();

    public void initializeAutoEcole(String name, String address, String phone, String email) {
        String sql = "INSERT INTO auto_ecole (name, address, telephone, email) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, phone);
            stmt.setString(4, email);

            stmt.executeUpdate();
            System.out.println("Auto-école enregistrée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'insertion : " + e.getMessage());
        }
    }





    public List<String[]> fetchAutoEcoleData() {
        List<String[]> dataList = new ArrayList<>();
        String sql = "SELECT name, address, telephone, email FROM auto_ecole";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String[] rowData = new String[4];
                rowData[0] = rs.getString("name");
                rowData[1] = rs.getString("address");
                rowData[2] = rs.getString("telephone");
                rowData[3] = rs.getString("email");
                dataList.add(rowData);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des données : " + e.getMessage());
        }
        return dataList;
    }


}
