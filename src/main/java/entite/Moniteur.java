package entite;

public class Moniteur {
    private int userId;
    private PermisType permisType;
    private double salaire; // New field for salary

    // Nested enum for permis type
    public static enum PermisType {
        A, B, C;
    }

    // Default constructor
    public Moniteur() {
    }

    // Constructor with all fields
    public Moniteur(int userId, PermisType permisType, double salaire) {
        this.userId = userId;
        this.permisType = permisType;
        this.salaire = salaire;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public PermisType getPermisType() {
        return permisType;
    }
    public void setPermisType(PermisType permisType) {
        this.permisType = permisType;
    }
    public double getSalaire() {
        return salaire;
    }
    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }
}
