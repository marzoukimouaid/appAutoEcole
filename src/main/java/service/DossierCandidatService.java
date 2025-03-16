package service;

import dao.DossierCandidatDao;
import dao.ProfileDao;
import dao.UserDao;
import Utils.ImgBBUtil;
import entite.DossierCandidat;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DossierCandidatService {

    private final DossierCandidatDao dossierDao = new DossierCandidatDao();
    private final ProfileDao profileDao = new ProfileDao(); // For profile deletion
    private final UserDao userDao = new UserDao();           // For user deletion

    /**
     * Creates a new dossier candidat. It uploads images (if provided) to ImgBB and sets their URLs.
     *
     * @param dossier The dossier candidat data (including new session counts).
     * @param cinFile The File for the CIN image.
     * @param certificatFile The File for the medical certificate image.
     * @param photoFile The File for the identity photo image.
     * @return true if creation was successful; false otherwise.
     */
    public boolean createDossier(DossierCandidat dossier, File cinFile, File certificatFile, File photoFile) {
        // Upload CIN file
        if (cinFile != null) {
            String uploadedCinUrl = ImgBBUtil.uploadImageToImgBB(cinFile);
            if (uploadedCinUrl != null) {
                dossier.setCinUrl(uploadedCinUrl);
            } else {
                System.err.println("CIN image upload failed. Aborting dossier creation.");
                return false;
            }
        } else {
            System.err.println("CIN file is required.");
            return false;
        }

        // Upload certificat file
        if (certificatFile != null) {
            String uploadedCertificatUrl = ImgBBUtil.uploadImageToImgBB(certificatFile);
            if (uploadedCertificatUrl != null) {
                dossier.setCertificatMedicalUrl(uploadedCertificatUrl);
            } else {
                System.err.println("Certificat image upload failed. Aborting dossier creation.");
                return false;
            }
        } else {
            System.err.println("Certificat file is required.");
            return false;
        }

        // Upload photo file
        if (photoFile != null) {
            String uploadedPhotoUrl = ImgBBUtil.uploadImageToImgBB(photoFile);
            if (uploadedPhotoUrl != null) {
                dossier.setPhotoIdentiteUrl(uploadedPhotoUrl);
            } else {
                System.err.println("Photo image upload failed. Aborting dossier creation.");
                return false;
            }
        } else {
            System.err.println("Photo file is required.");
            return false;
        }

        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        if (dossier.getCreatedAt() == null) {
            dossier.setCreatedAt(now);
        }
        dossier.setUpdatedAt(now);

        return dossierDao.createDossierCandidat(dossier);
    }

    /**
     * Retrieves the dossier candidat for a given candidate.
     */
    public Optional<DossierCandidat> getDossierByCandidateId(int candidateId) {
        return dossierDao.getDossierCandidatByCandidateId(candidateId);
    }

    /**
     * Retrieves all dossier candidats.
     */
    public List<DossierCandidat> getAllDossiers() {
        return dossierDao.getAllDossierCandidats();
    }

    /**
     * Deletes the dossier and associated records for a candidate.
     */
    public boolean deleteCandidateCascade(int candidateId) {
        boolean dossierDeleted = dossierDao.deleteDossierCandidate(candidateId);
        boolean profileDeleted = profileDao.deleteProfileByUserId(candidateId);
        boolean userDeleted = userDao.deleteUserById(candidateId);
        return dossierDeleted && profileDeleted && userDeleted;
    }
}
