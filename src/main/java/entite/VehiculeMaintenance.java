package entite;

import java.time.LocalDate;


public class VehiculeMaintenance {

    private int maintenanceId;
    private int vehiculeId;
    private LocalDate dateMaintenance;
    private String typeMaintenance;
    private String description;
    private double cost;
    private String scannedInvoiceUrl;

    public VehiculeMaintenance() {

    }

    public VehiculeMaintenance(int maintenanceId, int vehiculeId, LocalDate dateMaintenance,
                               String typeMaintenance, String description, double cost,
                               String scannedInvoiceUrl) {
        this.maintenanceId = maintenanceId;
        this.vehiculeId = vehiculeId;
        this.dateMaintenance = dateMaintenance;
        this.typeMaintenance = typeMaintenance;
        this.description = description;
        this.cost = cost;
        this.scannedInvoiceUrl = scannedInvoiceUrl;
    }


    public int getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(int maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public int getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(int vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public LocalDate getDateMaintenance() {
        return dateMaintenance;
    }

    public void setDateMaintenance(LocalDate dateMaintenance) {
        this.dateMaintenance = dateMaintenance;
    }

    public String getTypeMaintenance() {
        return typeMaintenance;
    }

    public void setTypeMaintenance(String typeMaintenance) {
        this.typeMaintenance = typeMaintenance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getScannedInvoiceUrl() {
        return scannedInvoiceUrl;
    }

    public void setScannedInvoiceUrl(String scannedInvoiceUrl) {
        this.scannedInvoiceUrl = scannedInvoiceUrl;
    }
}
