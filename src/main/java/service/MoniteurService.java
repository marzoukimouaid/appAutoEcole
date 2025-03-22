package service;

import dao.MoniteurDao;
import entite.Moniteur;
import dao.ProfileDao;
import dao.UserDao;
import entite.Profile;
import java.util.List;
import java.util.Optional;

public class MoniteurService {
    private final MoniteurDao moniteurDao = new MoniteurDao();
    private final ProfileDao profileDao = new ProfileDao();
    private final UserDao userDao = new UserDao();

    /**
     * Creates a new moniteur.
     */
    public boolean createMoniteur(Moniteur moniteur) {
        return moniteurDao.createMoniteur(moniteur);
    }

    /**
     * Retrieves a moniteur by user id.
     */
    public Optional<Moniteur> getMoniteurByUserId(int userId) {
        return moniteurDao.getMoniteurByUserId(userId);
    }

    /**
     * Updates an existing moniteur (only the moniteur-specific fields).
     */
    public boolean updateMoniteur(Moniteur moniteur) {
        return moniteurDao.updateMoniteur(moniteur);
    }

    /**
     * Updates an existing moniteur along with its associated profile.
     * This method updates the profile record (using ProfileService) and the moniteur record.
     *
     * @param moniteur The moniteur object with updated moniteur-specific fields.
     * @param profile  The profile object with updated profile fields.
     * @return True if both updates were successful; false otherwise.
     */
    public boolean updateMoniteur(Moniteur moniteur, Profile profile) {
        // Update profile first (no image file handling is needed here; pass null)
        boolean profileUpdated = new service.ProfileService().createOrUpdateProfile(profile, null);
        boolean moniteurUpdated = moniteurDao.updateMoniteur(moniteur);
        return profileUpdated && moniteurUpdated;
    }

    /**
     * Retrieves all moniteurs.
     */
    public List<Moniteur> getAllMoniteurs() {
        return moniteurDao.getAllMoniteurs();
    }

    /**
     * Deletes a moniteur along with their profile and user record (cascade deletion).
     */
    public boolean deleteMoniteurCascade(int userId) {
        boolean moniteurDeleted = moniteurDao.deleteMoniteurByUserId(userId);
        boolean profileDeleted = profileDao.deleteProfileByUserId(userId);
        boolean userDeleted = userDao.deleteUserById(userId);
        return (moniteurDeleted && profileDeleted && userDeleted);
    }
}
