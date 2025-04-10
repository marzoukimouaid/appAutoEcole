package entite;

import java.time.LocalDate;

public class Payment {
    private int id;
    private int userId;
    private double totalAmount;
    private String paymentType;
    private LocalDate paymentDate;
    private String status;

    public Payment() {}


    public Payment(int id, int userId, double totalAmount, String paymentType, LocalDate paymentDate, String status) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
        this.status = status;
    }


    public Payment(int userId, double totalAmount, String paymentType, LocalDate paymentDate, String status) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
        this.status = status;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public String getPaymentType() {
        return paymentType;
    }
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
