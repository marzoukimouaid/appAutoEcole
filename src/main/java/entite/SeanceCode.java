package entite;

import java.time.LocalDateTime;

public class SeanceCode {
    private int id;
    private int candidatId;
    private int moniteurId;
    private LocalDateTime sessionDatetime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SeanceCode() {}

    public SeanceCode(int id, int candidatId, int moniteurId, LocalDateTime sessionDatetime,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.sessionDatetime = sessionDatetime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public SeanceCode(int candidatId, int moniteurId, LocalDateTime sessionDatetime) {
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.sessionDatetime = sessionDatetime;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCandidatId() { return candidatId; }
    public void setCandidatId(int candidatId) { this.candidatId = candidatId; }
    public int getMoniteurId() { return moniteurId; }
    public void setMoniteurId(int moniteurId) { this.moniteurId = moniteurId; }
    public LocalDateTime getSessionDatetime() { return sessionDatetime; }
    public void setSessionDatetime(LocalDateTime sessionDatetime) { this.sessionDatetime = sessionDatetime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
