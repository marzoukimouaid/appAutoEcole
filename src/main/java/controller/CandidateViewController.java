package controller;

import entite.Profile;
import entite.DossierCandidat;
import service.ProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.time.format.DateTimeFormatter;

public class CandidateViewController {

    @FXML private Label lblTitle;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblBirthday;
    @FXML private Label lblPhone;
    @FXML private Label lblAddress;
    @FXML private Label lblPermisType;
    @FXML private Label lblJoinDate;
    @FXML private AnchorPane rootPane;

    @FXML private ImageView imgCin;
    @FXML private ImageView imgCertificat;
    @FXML private ImageView imgPhoto;

    private final ProfileService profileService = new ProfileService();

    
    public void initData(DossierCandidat dossier) {

        Profile profile = profileService.getProfileByUserId(dossier.getCandidateId()).orElse(null);

        if (profile != null) {
            lblName.setText(profile.getNom() + " " + profile.getPrenom());
            lblEmail.setText(profile.getEmail());
            lblBirthday.setText(profile.getBirthday() != null ? profile.getBirthday().toString() : "N/A");
            lblPhone.setText(String.valueOf(profile.getTel()));
            lblAddress.setText(profile.getAddresse());
        } else {
            lblName.setText("N/A");
            lblEmail.setText("N/A");
            lblBirthday.setText("N/A");
            lblPhone.setText("N/A");
            lblAddress.setText("N/A");
        }


        lblPermisType.setText(dossier.getPermisType());


        if (dossier.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            lblJoinDate.setText(dossier.getCreatedAt().format(formatter));
        } else {
            lblJoinDate.setText("N/A");
        }


        if (dossier.getCinUrl() != null && !dossier.getCinUrl().isEmpty()) {
            imgCin.setImage(new Image(dossier.getCinUrl(), true));
        }
        if (dossier.getCertificatMedicalUrl() != null && !dossier.getCertificatMedicalUrl().isEmpty()) {
            imgCertificat.setImage(new Image(dossier.getCertificatMedicalUrl(), true));
        }
        if (dossier.getPhotoIdentiteUrl() != null && !dossier.getPhotoIdentiteUrl().isEmpty()) {
            imgPhoto.setImage(new Image(dossier.getPhotoIdentiteUrl(), true));
        }
    }

    @FXML
    public void initialize() {

    }
}
