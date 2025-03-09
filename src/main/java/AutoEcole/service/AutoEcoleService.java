package AutoEcole.service;

import Authentication.dao.ProfileDao;
import Authentication.dao.UserDao;
import Authentication.entite.Profile;
import AutoEcole.dao.AutoEcoleDao;

import java.time.LocalDate;
import java.util.List;

public class AutoEcoleService {
    private static final AutoEcoleDao autoEcoleDao = new AutoEcoleDao();
    private static final UserDao userDao = new UserDao();
    private static final ProfileDao profileDao = new ProfileDao();

    public void initializeAutoEcole(String name, String address, String phone, String email) {
        autoEcoleDao.initializeAutoEcole(name, address, phone, email);

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
                defaultProfile.setDisplayName("Admin Secretaire");
                defaultProfile.setEmail("secretaire@autoecole.com");
                defaultProfile.setBirthday(LocalDate.of(1990, 1, 1));

                // Save the profile to the database
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
}
