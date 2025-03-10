package Candidat.entite;

import java.time.LocalDateTime;

public class DossierCandidat {
    private int id;
    private String cinUrl;
    private String certificatMedicalUrl;
    private String photoIdentiteUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int candidateId;
    private String permisType;

    // Default constructor
    public DossierCandidat() {
    }

    // Constructor without id (for creation, as id is auto-generated)
    public DossierCandidat(String cinUrl, String certificatMedicalUrl, String photoIdentiteUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt, int candidateId, String permisType) {
        this.cinUrl = cinUrl;
        this.certificatMedicalUrl = certificatMedicalUrl;
        this.photoIdentiteUrl = photoIdentiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.candidateId = candidateId;
        this.permisType = permisType;
    }

    // Full constructor with id
    public DossierCandidat(int id, String cinUrl, String certificatMedicalUrl, String photoIdentiteUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt, int candidateId, String permisType) {
        this.id = id;
        this.cinUrl = cinUrl;
        this.certificatMedicalUrl = certificatMedicalUrl;
        this.photoIdentiteUrl = photoIdentiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.candidateId = candidateId;
        this.permisType = permisType;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCinUrl() {
        return cinUrl;
    }
    public void setCinUrl(String cinUrl) {
        this.cinUrl = cinUrl;
    }
    public String getCertificatMedicalUrl() {
        return certificatMedicalUrl;
    }
    public void setCertificatMedicalUrl(String certificatMedicalUrl) {
        this.certificatMedicalUrl = certificatMedicalUrl;
    }
    public String getPhotoIdentiteUrl() {
        return photoIdentiteUrl;
    }
    public void setPhotoIdentiteUrl(String photoIdentiteUrl) {
        this.photoIdentiteUrl = photoIdentiteUrl;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public int getCandidateId() {
        return candidateId;
    }
    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }
    public String getPermisType() {
        return permisType;
    }
    public void setPermisType(String permisType) {
        this.permisType = permisType;
    }
}
