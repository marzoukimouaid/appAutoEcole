package dao;

import Utils.ConnexionDB;
import entite.Profile;

import java.sql.*;

public class ProfileDao {
    private final Connection conn = ConnexionDB.getInstance();

    
    public Profile getProfileByUserId(int userId) {
        String sql = "SELECT user_id, nom, prenom, email, picture_url, birthday, tel, addresse FROM profile WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Profile p = new Profile();
                p.setUserId(rs.getInt("user_id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setEmail(rs.getString("email"));
                p.setPictureUrl(rs.getString("picture_url"));
                Date dbDate = rs.getDate("birthday");
                if (dbDate != null) {
                    p.setBirthday(dbDate.toLocalDate());
                }
                p.setTel(rs.getInt("tel"));
                p.setAddresse(rs.getString("addresse"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public boolean createProfile(Profile profile) {
        if (profile.getPictureUrl() == null) {
            String sql = "INSERT INTO profile (user_id, nom, prenom, email, birthday, tel, addresse) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, profile.getUserId());
                stmt.setString(2, profile.getNom());
                stmt.setString(3, profile.getPrenom());
                stmt.setString(4, profile.getEmail());
                if (profile.getBirthday() != null) {
                    stmt.setDate(5, Date.valueOf(profile.getBirthday()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }
                stmt.setInt(6, profile.getTel());
                stmt.setString(7, profile.getAddresse());
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            String sql = "INSERT INTO profile (user_id, nom, prenom, email, picture_url, birthday, tel, addresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, profile.getUserId());
                stmt.setString(2, profile.getNom());
                stmt.setString(3, profile.getPrenom());
                stmt.setString(4, profile.getEmail());
                stmt.setString(5, profile.getPictureUrl());
                if (profile.getBirthday() != null) {
                    stmt.setDate(6, Date.valueOf(profile.getBirthday()));
                } else {
                    stmt.setNull(6, Types.DATE);
                }
                stmt.setInt(7, profile.getTel());
                stmt.setString(8, profile.getAddresse());
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    
    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE profile SET nom = ?, prenom = ?, email = ?, picture_url = ?, birthday = ?, tel = ?, addresse = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getNom());
            stmt.setString(2, profile.getPrenom());
            stmt.setString(3, profile.getEmail());
            stmt.setString(4, profile.getPictureUrl());
            if (profile.getBirthday() != null) {
                stmt.setDate(5, Date.valueOf(profile.getBirthday()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setInt(6, profile.getTel());
            stmt.setString(7, profile.getAddresse());
            stmt.setInt(8, profile.getUserId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public boolean deleteProfileByUserId(int userId) {
        String sql = "DELETE FROM profile WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
