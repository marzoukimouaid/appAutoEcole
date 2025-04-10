package controller;

import entite.Moniteur;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.ProfileService;

import java.time.format.DateTimeFormatter;

public class MoniteurViewController {

    @FXML private Label lblTitle;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblBirthday;
    @FXML private Label lblPhone;
    @FXML private Label lblAddress;
    @FXML private Label lblPermisType;
    @FXML private ImageView imgProfile;

    private final ProfileService profileService = new ProfileService();

    
    public void initData(Moniteur moniteur) {

        Profile profile = profileService.getProfileByUserId(moniteur.getUserId()).orElse(null);

        if (profile != null) {
            lblName.setText(profile.getNom() + " " + profile.getPrenom());
            lblEmail.setText(profile.getEmail());
            if (profile.getBirthday() != null) {
                lblBirthday.setText(profile.getBirthday().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            } else {
                lblBirthday.setText("N/A");
            }
            lblPhone.setText(String.valueOf(profile.getTel()));
            lblAddress.setText(profile.getAddresse());


            if (profile.getPictureUrl() != null && !profile.getPictureUrl().isEmpty()) {
                imgProfile.setImage(new Image(profile.getPictureUrl(), true));
            }
        } else {

            lblName.setText("N/A");
            lblEmail.setText("N/A");
            lblBirthday.setText("N/A");
            lblPhone.setText("N/A");
            lblAddress.setText("N/A");
        }


        if (moniteur.getPermisType() != null) {
            lblPermisType.setText(moniteur.getPermisType().name());
        } else {
            lblPermisType.setText("N/A");
        }
    }
}
