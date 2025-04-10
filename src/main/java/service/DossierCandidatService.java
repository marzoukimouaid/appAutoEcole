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
    private final ProfileDao profileDao = new ProfileDao();
    private final UserDao userDao = new UserDao();

    
    public boolean createDossier(DossierCandidat dossier, File cinFile, File certificatFile, File photoFile) {

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


        LocalDateTime now = LocalDateTime.now();
        if (dossier.getCreatedAt() == null) {
            dossier.setCreatedAt(now);
        }
        dossier.setUpdatedAt(now);

        return dossierDao.createDossierCandidat(dossier);
    }

    
    public Optional<DossierCandidat> getDossierByCandidateId(int candidateId) {
        return dossierDao.getDossierCandidatByCandidateId(candidateId);
    }

    
    public List<DossierCandidat> getAllDossiers() {
        return dossierDao.getAllDossierCandidats();
    }

    
    public boolean deleteCandidateCascade(int candidateId) {
        boolean dossierDeleted = dossierDao.deleteDossierCandidate(candidateId);
        boolean profileDeleted = profileDao.deleteProfileByUserId(candidateId);
        boolean userDeleted = userDao.deleteUserById(candidateId);
        return dossierDeleted && profileDeleted && userDeleted;
    }

    
    public boolean updateDossier(DossierCandidat dossier, File cinFile, File certificatFile, File photoFile) {
        if (cinFile != null) {
            String uploadedCinUrl = ImgBBUtil.uploadImageToImgBB(cinFile);
            if (uploadedCinUrl != null) {
                dossier.setCinUrl(uploadedCinUrl);
            }
        }
        if (certificatFile != null) {
            String uploadedCertificatUrl = ImgBBUtil.uploadImageToImgBB(certificatFile);
            if (uploadedCertificatUrl != null) {
                dossier.setCertificatMedicalUrl(uploadedCertificatUrl);
            }
        }
        if (photoFile != null) {
            String uploadedPhotoUrl = ImgBBUtil.uploadImageToImgBB(photoFile);
            if (uploadedPhotoUrl != null) {
                dossier.setPhotoIdentiteUrl(uploadedPhotoUrl);
            }
        }
        dossier.setUpdatedAt(LocalDateTime.now());
        return dossierDao.updateDossierCandidat(dossier);
    }
}
