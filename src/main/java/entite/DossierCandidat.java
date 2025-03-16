package entite;

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

    // New fields for the number of sessions
    private int nombreSeancesConduite;
    private int nombreSeancesCode;

    // Default constructor
    public DossierCandidat() {
    }

    // Constructor without id (for creation)
    public DossierCandidat(String cinUrl, String certificatMedicalUrl, String photoIdentiteUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt, int candidateId, String permisType,
                           int nombreSeancesConduite, int nombreSeancesCode) {
        this.cinUrl = cinUrl;
        this.certificatMedicalUrl = certificatMedicalUrl;
        this.photoIdentiteUrl = photoIdentiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.candidateId = candidateId;
        this.permisType = permisType;
        this.nombreSeancesConduite = nombreSeancesConduite;
        this.nombreSeancesCode = nombreSeancesCode;
    }

    // Full constructor with id
    public DossierCandidat(int id, String cinUrl, String certificatMedicalUrl, String photoIdentiteUrl,
                           LocalDateTime createdAt, LocalDateTime updatedAt, int candidateId, String permisType,
                           int nombreSeancesConduite, int nombreSeancesCode) {
        this.id = id;
        this.cinUrl = cinUrl;
        this.certificatMedicalUrl = certificatMedicalUrl;
        this.photoIdentiteUrl = photoIdentiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.candidateId = candidateId;
        this.permisType = permisType;
        this.nombreSeancesConduite = nombreSeancesConduite;
        this.nombreSeancesCode = nombreSeancesCode;
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
    public int getNombreSeancesConduite() {
        return nombreSeancesConduite;
    }
    public void setNombreSeancesConduite(int nombreSeancesConduite) {
        this.nombreSeancesConduite = nombreSeancesConduite;
    }
    public int getNombreSeancesCode() {
        return nombreSeancesCode;
    }
    public void setNombreSeancesCode(int nombreSeancesCode) {
        this.nombreSeancesCode = nombreSeancesCode;
    }
}
