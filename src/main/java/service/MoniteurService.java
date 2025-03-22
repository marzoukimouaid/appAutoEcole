package service;

import dao.MoniteurDao;
import entite.Moniteur;
import dao.ProfileDao;
import dao.UserDao;
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
     * Updates an existing moniteur.
     */
    public boolean updateMoniteur(Moniteur moniteur) {
        return moniteurDao.updateMoniteur(moniteur);
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
