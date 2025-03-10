package Candidat.service;

import Candidat.dao.DossierCandidatDao;
import Candidat.entite.DossierCandidat;

import java.util.Optional;

public class DossierCandidatService {
    private final DossierCandidatDao dossierDao = new DossierCandidatDao();

    /**
     * Creates a new dossier candidat.
     *
     * @param dossier The dossier candidat to create.
     * @return true if creation was successful; false otherwise.
     */
    public boolean createDossier(DossierCandidat dossier) {
        // Business validations could be added here before creation.
        return dossierDao.createDossierCandidat(dossier);
    }

    /**
     * Retrieves the dossier candidat for a given candidate.
     *
     * @param candidateId The candidate's id.
     * @return An Optional containing the dossier if found; otherwise, empty.
     */
    public Optional<DossierCandidat> getDossierByCandidateId(int candidateId) {
        return dossierDao.getDossierCandidatByCandidateId(candidateId);
    }

    // Additional service methods (update, delete, etc.) can be added as needed.
}
