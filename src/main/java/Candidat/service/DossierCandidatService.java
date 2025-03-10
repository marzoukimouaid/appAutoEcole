package Candidat.service;

import Candidat.dao.DossierCandidatDao;
import Candidat.entite.DossierCandidat;
import Utils.ImgBBUtil;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DossierCandidatService {

    private final DossierCandidatDao dossierDao = new DossierCandidatDao();

    /**
     * Creates a new dossier candidat. It uploads images (if provided) to ImgBB and sets their URLs.
     * All three files (CIN, certificat, photo) are required.
     * If any upload fails, the method returns false.
     *
     * @param dossier         The dossier candidat data (without image URLs).
     * @param cinFile         The File for the CIN image (required).
     * @param certificatFile  The File for the medical certificate image (required).
     * @param photoFile       The File for the identity photo image (required).
     * @return true if creation was successful; false otherwise.
     */
    public boolean createDossier(DossierCandidat dossier, File cinFile, File certificatFile, File photoFile) {
        // Ensure CIN file is provided and upload is successful
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

        // Ensure certificat file is provided and upload is successful
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

        // Ensure photo file is provided and upload is successful
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
     *
     * @param candidateId The candidate's id.
     * @return An Optional containing the dossier if found; otherwise, empty.
     */
    public Optional<DossierCandidat> getDossierByCandidateId(int candidateId) {
        return dossierDao.getDossierCandidatByCandidateId(candidateId);
    }

    /**
     * Retrieves all dossier candidats.
     *
     * @return A list of DossierCandidat.
     */
    public List<DossierCandidat> getAllDossiers() {
        return dossierDao.getAllDossierCandidats();
    }
}
