package entite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExamenCode {
    private int id;
    private int candidatId;
    private int moniteurId;
    private LocalDateTime examDatetime;
    private ExamStatus status;
    private double price;
    private PaymentStatus paiementStatus;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // NEW FIELD for notifications:
    private boolean notified;

    public enum ExamStatus {
        PENDING, PASSED, FAILED, ABSENT
    }

    public enum PaymentStatus {
        PAID, PENDING
    }

    // Constructors
    public ExamenCode() {
    }

    // Constructor often used for creation (sets defaults, etc.)
    public ExamenCode(int candidatId, int moniteurId, LocalDateTime examDatetime) {
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.examDatetime = examDatetime;
        this.status = ExamStatus.PENDING;
        this.price = 0.00;
        this.paiementStatus = PaymentStatus.PENDING;
        this.paymentDate = null;
        this.notified = false; // by default not notified
    }

    // You can have a full constructor if you wish
    public ExamenCode(int id, int candidatId, int moniteurId, LocalDateTime examDatetime,
                      ExamStatus status, double price, PaymentStatus paiementStatus,
                      LocalDate paymentDate, LocalDateTime createdAt, LocalDateTime updatedAt,
                      boolean notified) {
        this.id = id;
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.examDatetime = examDatetime;
        this.status = status;
        this.price = price;
        this.paiementStatus = paiementStatus;
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notified = notified;
    }

    // GETTERS AND SETTERS

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getCandidatId() {
        return candidatId;
    }
    public void setCandidatId(int candidatId) {
        this.candidatId = candidatId;
    }

    public int getMoniteurId() {
        return moniteurId;
    }
    public void setMoniteurId(int moniteurId) {
        this.moniteurId = moniteurId;
    }

    public LocalDateTime getExamDatetime() {
        return examDatetime;
    }
    public void setExamDatetime(LocalDateTime examDatetime) {
        this.examDatetime = examDatetime;
    }

    public ExamStatus getStatus() {
        return status;
    }
    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public PaymentStatus getPaiementStatus() {
        return paiementStatus;
    }
    public void setPaiementStatus(PaymentStatus paiementStatus) {
        this.paiementStatus = paiementStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
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

    public boolean isNotified() {
        return notified;
    }
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
