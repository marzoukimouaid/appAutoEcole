package entite;

import java.time.LocalDate;

/**
 * Entity mapping for the 'vehicule_maintenance' table.
 * This captures each maintenance or repair record for a vehicle.
 */
public class VehiculeMaintenance {

    private int maintenanceId;
    private int vehiculeId;
    private LocalDate dateMaintenance;
    private String typeMaintenance;   // e.g. "Frein", "Vidange", "Révision", etc.
    private String description;       // notes/description
    private double cost;              // cost if any
    private String scannedInvoiceUrl; // link to scanned invoice or receipt

    public VehiculeMaintenance() {
        // default constructor
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

    // Getters & Setters
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
