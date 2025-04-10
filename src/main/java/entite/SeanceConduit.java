package entite;

import java.time.LocalDateTime;

public class SeanceConduit {
    private int id;
    private int candidatId;
    private int moniteurId;
    private int vehiculeId;
    private LocalDateTime sessionDatetime;
    private double latitude;
    private double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SeanceConduit() {}

    public SeanceConduit(int id, int candidatId, int moniteurId, int vehiculeId, LocalDateTime sessionDatetime,
                         double latitude, double longitude, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.vehiculeId = vehiculeId;
        this.sessionDatetime = sessionDatetime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public SeanceConduit(int candidatId, int moniteurId, int vehiculeId, LocalDateTime sessionDatetime,
                         double latitude, double longitude) {
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.vehiculeId = vehiculeId;
        this.sessionDatetime = sessionDatetime;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCandidatId() { return candidatId; }
    public void setCandidatId(int candidatId) { this.candidatId = candidatId; }
    public int getMoniteurId() { return moniteurId; }
    public void setMoniteurId(int moniteurId) { this.moniteurId = moniteurId; }
    public int getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(int vehiculeId) { this.vehiculeId = vehiculeId; }
    public LocalDateTime getSessionDatetime() { return sessionDatetime; }
    public void setSessionDatetime(LocalDateTime sessionDatetime) { this.sessionDatetime = sessionDatetime; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
