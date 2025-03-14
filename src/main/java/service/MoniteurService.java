package service;

import dao.MoniteurDao;
import entite.Moniteur;
import dao.UserDao;
import dao.ProfileDao;

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
     * Updates an existing moniteur's permis type.
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
     * Deletes this moniteur, their profile, and their user record in one cascade.
     */
    public boolean deleteMoniteurCascade(int userId) {
        // 1) Delete from moniteurs table
        boolean moniteurDeleted = moniteurDao.deleteMoniteurByUserId(userId);

        // 2) Delete the associated profile
        boolean profileDeleted = profileDao.deleteProfileByUserId(userId);

        // 3) Delete from users table
        boolean userDeleted = userDao.deleteUserById(userId);

        // You can refine success logic if you wish
        return (moniteurDeleted && profileDeleted && userDeleted);
    }
}
