package Authentication.controller;

import Authentication.entite.Profile;
import Authentication.service.ProfileService;
import Utils.NotificationUtil;
import Utils.SessionManager;
import Authentication.entite.User;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class ProfileController {

    @FXML private ImageView profileImage;
    @FXML private TextField displayNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private Button uploadImageButton;
    @FXML private StackPane rootPane;
    private final ProfileService profileService = new ProfileService();
    private Profile userProfile;
    private User currentUser;
    private File selectedImageFile = null; // Store selected image for later upload

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


        // Fill fields if data found
        displayNameField.setText(userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "");
        emailField.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "");
        if (userProfile.getBirthday() != null) {
            birthdayPicker.setValue(userProfile.getBirthday());
        }

        // Display existing profile image
        if (userProfile.getPictureUrl() != null && !userProfile.getPictureUrl().isEmpty()) {
            profileImage.setImage(new Image(userProfile.getPictureUrl(), true));
        }
    }

    private void makeProfileImageCircular() {
        double radius = profileImage.getFitWidth() / 2;
        Circle clip = new Circle(radius, radius, radius);
        profileImage.setClip(clip);
    }


    /**
     * Opens a file chooser to select an image (but does not upload yet).
     */
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (selectedImageFile != null) {
            System.out.println("Selected file: " + selectedImageFile.getAbsolutePath());
            profileImage.setImage(new Image(selectedImageFile.toURI().toString())); // Show preview
        }
    }

    /**
     * Saves the profile (and uploads the image if a new one was selected).
     */
    @FXML
    private void handleSaveProfile() {
        userProfile.setDisplayName(displayNameField.getText().trim());
        userProfile.setEmail(emailField.getText().trim());
        if (birthdayPicker.getValue() != null) {
            userProfile.setBirthday(birthdayPicker.getValue());
        } else {
            userProfile.setBirthday(null);
        }

        // Save profile and upload image if needed
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
