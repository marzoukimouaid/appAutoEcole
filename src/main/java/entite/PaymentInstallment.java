package entite;

import java.time.LocalDate;

public class PaymentInstallment {

    public enum Status {
        PENDING, PAID
    }

    private int installmentId;
    private int paymentId;           // Foreign key to Payment
    private int installmentNumber;   // e.g., 1 for first month, 2 for second, etc.
    private LocalDate dueDate;       // Due date for this installment
    private double amountDue;        // The amount that should be paid in this installment
    private Status status;           // "PENDING" or "PAID"
    private LocalDate datePaid;      // Date when installment was paid (null if pending)
    private boolean notified;        // New field: whether a notification has been sent

    public PaymentInstallment() {}

    // Full constructor (including id and notified)
    public PaymentInstallment(int installmentId, int paymentId, int installmentNumber, LocalDate dueDate,
                              double amountDue, Status status, LocalDate datePaid, boolean notified) {
        this.installmentId = installmentId;
        this.paymentId = paymentId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.status = status;
        this.datePaid = datePaid;
        this.notified = notified;
    }

    // Constructor for creation (without id and datePaid); notified defaults to false
    public PaymentInstallment(int paymentId, int installmentNumber, LocalDate dueDate, double amountDue, Status status) {
        this.paymentId = paymentId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.status = status;
        this.datePaid = null;
        this.notified = false;
    }

    // Getters and Setters
    public int getInstallmentId() {
        return installmentId;
    }
    public void setInstallmentId(int installmentId) {
        this.installmentId = installmentId;
    }
    public int getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }
    public int getInstallmentNumber() {
        return installmentNumber;
    }
    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public double getAmountDue() {
        return amountDue;
    }
    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public LocalDate getDatePaid() {
        return datePaid;
    }
    public void setDatePaid(LocalDate datePaid) {
        this.datePaid = datePaid;
    }
    public boolean isNotified() {
        return notified;
    }
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
