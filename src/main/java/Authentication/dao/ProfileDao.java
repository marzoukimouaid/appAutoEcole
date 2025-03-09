package Authentication.dao;

import Utils.ConnexionDB;
import Authentication.entite.Profile;
import java.sql.*;
import java.time.LocalDate;

public class ProfileDao {
    private final Connection conn = ConnexionDB.getInstance();

    public Profile getProfileByUserId(int userId) {
        String sql = "SELECT user_id, display_name, email, picture_url, birthday FROM profile WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Profile p = new Profile();
                p.setUserId(rs.getInt("user_id"));
                p.setDisplayName(rs.getString("display_name"));
                p.setEmail(rs.getString("email"));
                p.setPictureUrl(rs.getString("picture_url"));

                Date dbDate = rs.getDate("birthday");
                if (dbDate != null) {
                    p.setBirthday(dbDate.toLocalDate());
                }
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createProfile(Profile profile) {
        String sql = "INSERT INTO profile (user_id, display_name, email, birthday) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getDisplayName());
            stmt.setString(3, profile.getEmail());
            if (profile.getBirthday() != null) {
                stmt.setDate(4, Date.valueOf(profile.getBirthday()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE profile SET display_name = ?, email = ?, picture_url = ?, birthday = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, profile.getDisplayName());
            stmt.setString(2, profile.getEmail());
            stmt.setString(3, profile.getPictureUrl());
            if (profile.getBirthday() != null) {
                stmt.setDate(4, Date.valueOf(profile.getBirthday()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setInt(5, profile.getUserId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
