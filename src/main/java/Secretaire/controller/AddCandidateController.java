package Secretaire.controller;

import Authentication.entite.Profile;
import Authentication.service.ProfileService;
import Authentication.service.UserService;
import Candidat.entite.DossierCandidat;
import Candidat.service.DossierCandidatService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

public class AddCandidateController {

    // User fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Profile fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    // Dossier candidate fields
    @FXML private ComboBox<String> permisTypeComboBox;
    @FXML private Button btnChooseCIN;
    @FXML private Button btnChooseCertificat;
    @FXML private Button btnChoosePhoto;

    @FXML private Button btnSubmit;

    // Error labels
    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label birthdayError;
    @FXML private Label phoneError;
    @FXML private Label addressError;
    @FXML private Label permisTypeError;
    @FXML private Label cinError;
    @FXML private Label certificatError;
    @FXML private Label photoError;

    // Selected files for dossier candidate
    private File cinFile;
    private File certificatFile;
    private File photoFile;

    // Services – following layered architecture
    private final UserService userService = new UserService();
    private final ProfileService profileService = new ProfileService();
    private final DossierCandidatService dossierService = new DossierCandidatService();

    @FXML
    public void initialize() {
        // Initialize permis type drop-down with example values
        permisTypeComboBox.getItems().addAll("A", "B", "C");

        // Add listeners to remove error styling when the user starts typing/changes value for text fields
        addClearErrorListener(usernameField, usernameError);
        addClearErrorListener(passwordField, passwordError);
        addClearErrorListener(firstNameField, firstNameError);
        addClearErrorListener(lastNameField, lastNameError);
        addClearErrorListener(emailField, emailError);

        // For DatePicker and ComboBox, clear error style as well as error label
        birthdayPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            birthdayPicker.getStyleClass().remove("error");
            birthdayError.setText("");
        });
        addClearErrorListener(phoneField, phoneError);
        addClearErrorListener(addressField, addressError);
        permisTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            permisTypeComboBox.getStyleClass().remove("error");
            permisTypeError.setText("");
        });

        // File chooser handlers also clear errors on valid selection
        btnChooseCIN.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select CIN Image");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File selected = fileChooser.showOpenDialog(btnChooseCIN.getScene().getWindow());
            if (selected != null) {
                if (isImageFile(selected)) {
                    cinFile = selected;
                    btnChooseCIN.setText(selected.getName());
                    cinError.setText("");
                } else {
                    setFieldError(btnChooseCIN, cinError, "Invalid file");
                }
            }
        });

        btnChooseCertificat.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Certificat Image");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File selected = fileChooser.showOpenDialog(btnChooseCertificat.getScene().getWindow());
            if (selected != null) {
                if (isImageFile(selected)) {
                    certificatFile = selected;
                    btnChooseCertificat.setText(selected.getName());
                    certificatError.setText("");
                } else {
                    setFieldError(btnChooseCertificat, certificatError, "Invalid file");
                }
            }
        });

        btnChoosePhoto.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Photo Image");
            fileChooser.getExtensionFilters().clear();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File selected = fileChooser.showOpenDialog(btnChoosePhoto.getScene().getWindow());
            if (selected != null) {
                if (isImageFile(selected)) {
                    photoFile = selected;
                    btnChoosePhoto.setText(selected.getName());
                    photoError.setText("");
                } else {
                    setFieldError(btnChoosePhoto, photoError, "Invalid file");
                }
            }
        });

        btnSubmit.setOnAction(this::handleSubmit);
    }

    private void handleSubmit(ActionEvent event) {
        clearAllErrors();
        boolean valid = true;

        // Validate username
        if (usernameField.getText().trim().isEmpty()) {
            setFieldError(usernameField, usernameError, "Username required");
            valid = false;
        }
        // Validate password
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            setFieldError(passwordField, passwordError, "Password required");
            valid = false;
        } else if (!isValidPassword(password)) {
            setFieldError(passwordField, passwordError, "Min 8 chars with upper, lower, digit & special");
            valid = false;
        }
        // Validate first name
        if (firstNameField.getText().trim().isEmpty()) {
            setFieldError(firstNameField, firstNameError, "First name required");
            valid = false;
        }
        // Validate last name
        if (lastNameField.getText().trim().isEmpty()) {
            setFieldError(lastNameField, lastNameError, "Last name required");
            valid = false;
        }
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            setFieldError(emailField, emailError, "Email required");
            valid = false;
        } else if (!isValidEmail(email)) {
            setFieldError(emailField, emailError, "Invalid email");
            valid = false;
        }
        // Validate birthday
        if (birthdayPicker.getValue() == null) {
            setFieldError(birthdayPicker, birthdayError, "Birthday required");
            valid = false;
        } else {
            LocalDate birthday = birthdayPicker.getValue();
            LocalDate today = LocalDate.now();
            if (birthday.isAfter(today)) {
                setFieldError(birthdayPicker, birthdayError, "Cannot be future date");
                valid = false;
            } else {
                int age = Period.between(birthday, today).getYears();
                if (age < 18) {
                    setFieldError(birthdayPicker, birthdayError, "Must be 18+");
                    valid = false;
                }
            }
        }
        // Validate phone number
        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{8}")) {
            setFieldError(phoneField, phoneError, "8-digit number required");
            valid = false;
        }
        // Validate address
        if (addressField.getText().trim().isEmpty()) {
            setFieldError(addressField, addressError, "Address required");
            valid = false;
        }
        // Validate permis type
        if (permisTypeComboBox.getValue() == null) {
            setFieldError(permisTypeComboBox, permisTypeError, "Select permis type");
            valid = false;
        }
        // Validate file selections
        if (cinFile == null) {
            cinError.setText("CIN image required");
            valid = false;
        }
        if (certificatFile == null) {
            certificatError.setText("Certificat image required");
            valid = false;
        }
        if (photoFile == null) {
            photoError.setText("Photo image required");
            valid = false;
        }
        if (!valid) {
            return;
        }

        // Create candidate
        String username = usernameField.getText().trim();
        boolean userCreated = userService.createUser(username, password, "candidat");
        if (!userCreated) {
            setFieldError(usernameField, usernameError, "Username might exist");
            return;
        }
        int userId = userService.getUserIdByUsername(username);
        if (userId == -1) {
            setFieldError(usernameField, usernameError, "User retrieval error");
            return;
        }
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setNom(firstNameField.getText().trim());
        profile.setPrenom(lastNameField.getText().trim());
        profile.setEmail(email);
        profile.setBirthday(birthdayPicker.getValue());
        try {
            profile.setTel(Integer.parseInt(phone));
        } catch (NumberFormatException e) {
            profile.setTel(0);
        }
        profile.setAddresse(addressField.getText().trim());
        boolean profileCreated = profileService.createOrUpdateProfile(profile, null);
        if (!profileCreated) {
            setFieldError(emailField, emailError, "Profile creation error");
            return;
        }
        DossierCandidat dossier = new DossierCandidat();
        dossier.setCandidateId(userId);
        dossier.setPermisType(permisTypeComboBox.getValue());
        LocalDateTime now = LocalDateTime.now();
        dossier.setCreatedAt(now);
        dossier.setUpdatedAt(now);
        boolean dossierCreated = dossierService.createDossier(dossier, cinFile, certificatFile, photoFile);
        if (!dossierCreated) {
            showInlineError("Dossier creation error");
            return;
        }
        showInlineSuccess("Candidate added successfully!");
        clearForm();
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        birthdayPicker.setValue(null);
        phoneField.clear();
        addressField.clear();
        permisTypeComboBox.getSelectionModel().clearSelection();
        btnChooseCIN.setText("Choisir le fichier");
        btnChooseCertificat.setText("Choisir le fichier");
        btnChoosePhoto.setText("Choisir le fichier");
        cinFile = null;
        certificatFile = null;
        photoFile = null;
        clearAllErrors();
    }

    private void clearAllErrors() {
        usernameField.getStyleClass().remove("error");
        passwordField.getStyleClass().remove("error");
        firstNameField.getStyleClass().remove("error");
        lastNameField.getStyleClass().remove("error");
        emailField.getStyleClass().remove("error");
        birthdayPicker.getStyleClass().remove("error");
        phoneField.getStyleClass().remove("error");
        addressField.getStyleClass().remove("error");
        permisTypeComboBox.getStyleClass().remove("error");

        usernameError.setText("");
        passwordError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        birthdayError.setText("");
        phoneError.setText("");
        addressError.setText("");
        permisTypeError.setText("");
        cinError.setText("");
        certificatError.setText("");
        photoError.setText("");
    }

    private void setFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    private void addClearErrorListener(TextField field, Label errorLabel) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            field.getStyleClass().remove("error");
            if (errorLabel != null) {
                errorLabel.setText("");
            }
        });
    }

    // Helper method for file upload errors may be handled in their onAction handlers

    private void showInlineError(String message) {
        System.err.println(message);
    }

    private void showInlineSuccess(String message) {
        System.out.println(message);
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.matches(pattern, password);
    }

    private boolean isValidEmail(String email) {
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(pattern, email);
    }

    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }
}
