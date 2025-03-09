package Authentication.entite;

import java.time.LocalDate;

public class Profile {
    private int userId;
    private String displayName;
    private String email;
    private String pictureUrl;
    private LocalDate birthday;

    public Profile() {}

    public Profile(int userId, String displayName, String email, String pictureUrl, LocalDate birthday) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.birthday = birthday;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
