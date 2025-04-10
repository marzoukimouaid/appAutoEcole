package controller;

import entite.Profile;
import service.ProfileService;
import Utils.NotificationUtil;
import Utils.SessionManager;
import entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;

public class ProfileController {

    @FXML private ImageView profileImage;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField telField;
    @FXML private TextField addresseField;
    @FXML private Button uploadImageButton;
    @FXML private StackPane rootPane;

    private final ProfileService profileService = new ProfileService();
    private Profile userProfile;
    private User currentUser;
    private File selectedImageFile = null;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user in session. Cannot load profile.");
            return;
        }

        userProfile = profileService.getProfileByUserId(currentUser.getId()).orElseGet(() -> {
            Profile newProfile = new Profile();
            newProfile.setUserId(currentUser.getId());
            return newProfile;
        });


        nomField.setText(userProfile.getNom());
        prenomField.setText(userProfile.getPrenom());
        emailField.setText(userProfile.getEmail());
        if (userProfile.getBirthday() != null) {
            birthdayPicker.setValue(userProfile.getBirthday());
        }
        telField.setText(String.valueOf(userProfile.getTel()));
        addresseField.setText(userProfile.getAddresse());

        if (userProfile.getPictureUrl() != null && !userProfile.getPictureUrl().isEmpty()) {
            profileImage.setImage(new Image(userProfile.getPictureUrl(), true));
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (selectedImageFile != null) {
            profileImage.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    private void handleSaveProfile() {
        userProfile.setNom(nomField.getText().trim());
        userProfile.setPrenom(prenomField.getText().trim());
        userProfile.setEmail(emailField.getText().trim());

        if (birthdayPicker.getValue() != null) {
            userProfile.setBirthday(birthdayPicker.getValue());
        } else {
            userProfile.setBirthday(null);
        }


        try {
            int tel = Integer.parseInt(telField.getText().trim());
            userProfile.setTel(tel);
        } catch (NumberFormatException e) {
            userProfile.setTel(0);
        }
        userProfile.setAddresse(addresseField.getText().trim());

        boolean success = profileService.createOrUpdateProfile(userProfile, selectedImageFile);
        if (success) {
            NotificationUtil.showNotification(
                    rootPane,
                    "Profile updated successfully!",
                    NotificationUtil.NotificationType.SUCCESS
            );
        } else {
            NotificationUtil.showNotification(
                    rootPane,
                    "Error saving your profile. Please try again.",
                    NotificationUtil.NotificationType.ERROR
            );
        }
    }
}
