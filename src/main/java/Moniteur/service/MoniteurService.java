package Moniteur.service;

import Moniteur.dao.MoniteurDao;
import Moniteur.entite.Moniteur;

import java.util.List;
import java.util.Optional;

public class MoniteurService {
    private final MoniteurDao moniteurDao = new MoniteurDao();

    /**
     * Creates a new moniteur.
     * @param moniteur the moniteur to create.
     * @return true if creation is successful.
     */
    public boolean createMoniteur(Moniteur moniteur) {
        return moniteurDao.createMoniteur(moniteur);
    }

    /**
     * Retrieves a moniteur by user id.
     * @param userId the user id.
     * @return an Optional containing the moniteur if found.
     */
    public Optional<Moniteur> getMoniteurByUserId(int userId) {
        return moniteurDao.getMoniteurByUserId(userId);
    }

    /**
     * Updates an existing moniteur's permis type.
     * @param moniteur the moniteur with updated data.
     * @return true if the update is successful.
     */
    public boolean updateMoniteur(Moniteur moniteur) {
        return moniteurDao.updateMoniteur(moniteur);
    }

    /**
     * Retrieves all moniteurs.
     * @return a list of Moniteur.
     */
    public List<Moniteur> getAllMoniteurs() {
        return moniteurDao.getAllMoniteurs();
    }
}
