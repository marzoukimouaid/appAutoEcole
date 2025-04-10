package entite;

import java.time.LocalDate;


public class VehiculeDocument {

    public enum DocType {
        VIGNETTE,
        VISITE_TECHNIQUE,
        ASSURANCE,
        VIDANGE
    }

    private int docId;
    private int vehiculeId;
    private DocType docType;
    private LocalDate dateObtention;
    private LocalDate dateExpiration;
    private String scannedDocUrl;
    private double cost;
    private boolean notified;

    public VehiculeDocument() {

    }

    public VehiculeDocument(int docId, int vehiculeId, DocType docType,
                            LocalDate dateObtention, LocalDate dateExpiration,
                            String scannedDocUrl, double cost, boolean notified) {
        this.docId = docId;
        this.vehiculeId = vehiculeId;
        this.docType = docType;
        this.dateObtention = dateObtention;
        this.dateExpiration = dateExpiration;
        this.scannedDocUrl = scannedDocUrl;
        this.cost = cost;
        this.notified = notified;
    }


    public int getDocId() {
        return docId;
    }
    public void setDocId(int docId) {
        this.docId = docId;
    }
    public int getVehiculeId() {
        return vehiculeId;
    }
    public void setVehiculeId(int vehiculeId) {
        this.vehiculeId = vehiculeId;
    }
    public DocType getDocType() {
        return docType;
    }
    public void setDocType(DocType docType) {
        this.docType = docType;
    }
    public LocalDate getDateObtention() {
        return dateObtention;
    }
    public void setDateObtention(LocalDate dateObtention) {
        this.dateObtention = dateObtention;
    }
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }
    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    public String getScannedDocUrl() {
        return scannedDocUrl;
    }
    public void setScannedDocUrl(String scannedDocUrl) {
        this.scannedDocUrl = scannedDocUrl;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public boolean isNotified() {
        return notified;
    }
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
