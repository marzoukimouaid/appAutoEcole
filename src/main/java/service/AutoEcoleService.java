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


    public void initializeAutoEcole(String name, String address, String phone, String email,
                                    double prixSeanceConduit, double prixSeanceCode) {
        autoEcoleDao.initializeAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);


        int secretaireId = userDao.getUserIdByUsername("secretaire");

        if (secretaireId == -1) {

            boolean userCreated = userDao.createUser("secretaire", "secretaire", "secretaire");
            if (userCreated) {
                secretaireId = userDao.getUserIdByUsername("secretaire");
            }
        }

        if (secretaireId > 0) {

            Profile existingProfile = profileDao.getProfileByUserId(secretaireId);
            if (existingProfile == null) {

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


    public static String getAutoEcoleName() {
        List<String[]> data = getAutoEcoleData();
        if (!data.isEmpty()) {
            return data.get(0)[0];
        }
        return "";
    }

    
    public void updateAutoEcole(String name, String address, String phone, String email,
                                double prixSeanceConduit, double prixSeanceCode) {
        autoEcoleDao.updateAutoEcole(name, address, phone, email, prixSeanceConduit, prixSeanceCode);
    }
}
