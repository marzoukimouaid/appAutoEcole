package entite;

import java.time.LocalDate;

public class Vehicule {
    private int id;
    private String immatriculation;
    private String marque;
    private LocalDate dateMiseEnService;
    private int kilometrageTotal;
    private int kmRestantEntretien;
    private VehicleType type;

    // Nested enum for vehicle type
    public static enum VehicleType {
        MOTO, VOITURE, CAMION;
    }

    public Vehicule() {}

    // Constructor for creation (without id)
    public Vehicule(String immatriculation, String marque, LocalDate dateMiseEnService, int kilometrageTotal, int kmRestantEntretien, VehicleType type) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageTotal = kilometrageTotal;
        this.kmRestantEntretien = kmRestantEntretien;
        this.type = type;
    }

    // Full constructor with id
    public Vehicule(int id, String immatriculation, String marque, LocalDate dateMiseEnService, int kilometrageTotal, int kmRestantEntretien, VehicleType type) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageTotal = kilometrageTotal;
        this.kmRestantEntretien = kmRestantEntretien;
        this.type = type;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public LocalDate getDateMiseEnService() { return dateMiseEnService; }
    public void setDateMiseEnService(LocalDate dateMiseEnService) { this.dateMiseEnService = dateMiseEnService; }

    public int getKilometrageTotal() { return kilometrageTotal; }
    public void setKilometrageTotal(int kilometrageTotal) { this.kilometrageTotal = kilometrageTotal; }

    public int getKmRestantEntretien() { return kmRestantEntretien; }
    public void setKmRestantEntretien(int kmRestantEntretien) { this.kmRestantEntretien = kmRestantEntretien; }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
}
