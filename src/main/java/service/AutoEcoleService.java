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
                defaultProfile.setNom("secretaire");
                defaultProfile.setPrenom("Ben foulen");
                defaultProfile.setEmail("secretaire@autoecole.com");
                defaultProfile.setBirthday(LocalDate.of(1990, 1, 1));
                // Do not set pictureUrl so that SQL default is used
                // Set a dummy address since the database doesn't have a default for it
                defaultProfile.setAddresse("Default Address");
                // Optionally, you can also set a default telephone number if needed
                // defaultProfile.setTel(0);  // or any dummy value

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
