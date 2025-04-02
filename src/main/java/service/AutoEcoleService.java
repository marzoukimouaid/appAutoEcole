package service;

import dao.ProfileDao;
import dao.UserDao;
import entite.Profile;
import dao.AutoEcoleDao;
import java.time.LocalDate;
import java.util.List;

public class AutoEcoleService {
    private static final AutoEcoleDao autoEcoleDao = new AutoEcoleDao();
    private static final UserDao userDao = new UserDao();
    private static final ProfileDao profileDao = new ProfileDao();

    // Updated method with two new parameters: prixSeanceConduit and prixSeanceCode.
    public void initializeAutoEcole(String name, String address, String phone, String email,
                                    double prixSeanceConduit, double prixSeanceCode) {
        autoEcoleDao.initializeAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);

        // Check if the secretaire user already exists
        int secretaireId = userDao.getUserIdByUsername("secretaire");

        if (secretaireId == -1) {
            // Create a new secretaire user
            boolean userCreated = userDao.createUser("secretaire", "secretaire", "secretaire");
            if (userCreated) {
                secretaireId = userDao.getUserIdByUsername("secretaire");
            }
        }

        if (secretaireId > 0) {
            // Check if the profile already exists
            Profile existingProfile = profileDao.getProfileByUserId(secretaireId);
            if (existingProfile == null) {
                // Create a default profile for the secretaire
                Profile defaultProfile = new Profile();
                defaultProfile.setUserId(secretaireId);
                defaultProfile.setNom("secretaire");
                defaultProfile.setPrenom("Ben foulen");
                defaultProfile.setEmail("secretaire@autoecole.com");
                defaultProfile.setBirthday(LocalDate.of(1990, 1, 1));
                defaultProfile.setAddresse("Default Address");
                profileDao.createProfile(defaultProfile);
                System.out.println("Secretaire profile created successfully.");
            } else {
                System.out.println("Secretaire profile already exists.");
            }
        }
    }

    public static List<String[]> getAutoEcoleData() {
        return autoEcoleDao.fetchAutoEcoleData();
    }

    // New method to get the auto-école name from the database
    public static String getAutoEcoleName() {
        List<String[]> data = getAutoEcoleData();
        if (!data.isEmpty()) {
            return data.get(0)[0];
        }
        return "";
    }

    /**
     * New method to update the auto-école configuration.
     */
    public void updateAutoEcole(String name, String address, String phone, String email,
                                double prixSeanceConduit, double prixSeanceCode) {
        autoEcoleDao.updateAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);
    }
}
