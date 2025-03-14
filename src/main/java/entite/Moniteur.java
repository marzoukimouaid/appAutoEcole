package entite;

/**
 * The Moniteur entity represents a driving instructor.
 * The user_id serves as both the foreign key to the users table and the primary key for this table.
 */
public class Moniteur {
    private int userId;
    private PermisType permisType;

    // Nested enum for permis type (without a separate class)
    public static enum PermisType {
        A, B, C;
    }

    public Moniteur() {
    }

    public Moniteur(int userId, PermisType permisType) {
        this.userId = userId;
        this.permisType = permisType;
    }

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
}
