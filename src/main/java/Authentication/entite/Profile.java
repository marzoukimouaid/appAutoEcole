package Authentication.entite;

import java.time.LocalDate;

public class Profile {
    private int userId;
    private String nom;
    private String prenom;
    private String email;
    private String pictureUrl;
    private LocalDate birthday;
    private int tel;
    private String addresse;

    public Profile() {}

    public Profile(int userId, String nom, String prenom, String email, String pictureUrl, LocalDate birthday, int tel, String addresse) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.birthday = birthday;
        this.tel = tel;
        this.addresse = addresse;
    }


    public int getTel() {
        return tel;
    }

    public void setTel(int tel) {
        this.tel = tel;
    }

    public String getAddresse() {
        return addresse;
    }

    public void setAddresse(String addresse) {
        this.addresse = addresse;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
