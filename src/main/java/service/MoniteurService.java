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

    
    public boolean createMoniteur(Moniteur moniteur) {
        return moniteurDao.createMoniteur(moniteur);
    }

    
    public Optional<Moniteur> getMoniteurByUserId(int userId) {
        return moniteurDao.getMoniteurByUserId(userId);
    }

    
    public boolean updateMoniteur(Moniteur moniteur) {
        return moniteurDao.updateMoniteur(moniteur);
    }

    
    public boolean updateMoniteur(Moniteur moniteur, Profile profile) {

        boolean profileUpdated = new service.ProfileService().createOrUpdateProfile(profile, null);
        boolean moniteurUpdated = moniteurDao.updateMoniteur(moniteur);
        return profileUpdated && moniteurUpdated;
    }

    
    public List<Moniteur> getAllMoniteurs() {
        return moniteurDao.getAllMoniteurs();
    }

    
    public boolean deleteMoniteurCascade(int userId) {
        boolean moniteurDeleted = moniteurDao.deleteMoniteurByUserId(userId);
        boolean profileDeleted = profileDao.deleteProfileByUserId(userId);
        boolean userDeleted = userDao.deleteUserById(userId);
        return (moniteurDeleted && profileDeleted && userDeleted);
    }
}
