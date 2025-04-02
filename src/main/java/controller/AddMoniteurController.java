package controller;

import entite.Profile;
import entite.Moniteur;
import entite.Moniteur.PermisType;
import entite.User;
import service.ProfileService;
import service.UserService;
import service.MoniteurService;
import Utils.NotificationUtil;
import Utils.NotificationUtil.NotificationType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class AddMoniteurController {

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

    // Moniteur specific fields
    @FXML private ComboBox<String> permisTypeComboBox;
    @FXML private TextField salaireField; // New field for salary

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
    @FXML private Label salaireError; // New error label for salary

    // Root container for notifications – ensure your FXML has this defined.
    @FXML private StackPane rootPane;

    // Services – following layered architecture
    private final UserService userService = new UserService();
    private final ProfileService profileService = new ProfileService();
    private final MoniteurService moniteurService = new MoniteurService();

    // Fields for edit mode
    private boolean isEditMode = false;
    private Moniteur editingMoniteur;
    private Profile editingProfile;

    @FXML
    public void initialize() {
        // Initialize permis type drop-down with values A, B, and C.
        permisTypeComboBox.getItems().addAll("A", "B", "C");

        // Add listeners to remove error styling when the user changes text values.
        addClearErrorListener(usernameField, usernameError);
        addClearErrorListener(passwordField, passwordError);
        addClearErrorListener(firstNameField, firstNameError);
        addClearErrorListener(lastNameField, lastNameError);
        addClearErrorListener(emailField, emailError);

        // For DatePicker and ComboBox, clear error style and error label on change.
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
        addClearErrorListener(salaireField, salaireError); // Add listener for salary field

        btnSubmit.setOnAction(this::handleSubmit);
    }

    /**
     * Initializes the form for editing a moniteur.
     * Preloads the moniteur's existing data into the inputs.
     *
     * @param moniteur The moniteur record to edit.
     * @param profile  The associated profile record.
     */
    public void initData(Moniteur moniteur, Profile profile) {
        isEditMode = true;
        editingMoniteur = moniteur;
        editingProfile = profile;

        // Preload user data: retrieve existing user and set username.
        User existingUser = userService.getUserById(profile.getUserId());
        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            usernameField.setDisable(true);
        }
        // Leave password empty (if provided, it will update the password via your service if implemented)
        passwordField.clear();

        // Preload profile fields
        firstNameField.setText(profile.getNom());
        lastNameField.setText(profile.getPrenom());
        emailField.setText(profile.getEmail());
        birthdayPicker.setValue(profile.getBirthday());
        phoneField.setText(String.valueOf(profile.getTel()));
        addressField.setText(profile.getAddresse());

        // Preload moniteur fields
        permisTypeComboBox.setValue(moniteur.getPermisType().name());
        salaireField.setText(String.valueOf(moniteur.getSalaire()));

        // Update the submit button text to reflect edit mode.
        btnSubmit.setText("Mettre à jour Moniteur");
    }

    private void handleSubmit(ActionEvent event) {
        clearAllErrors();
        boolean valid = true;

        // Validate username.
        if (usernameField.getText().trim().isEmpty()) {
            setFieldError(usernameField, usernameError, "Username required");
            valid = false;
        }
        // Validate password.
        String password = passwordField.getText().trim();
        if (!isEditMode) {
            // In create mode, password is required.
            if (password.isEmpty()) {
                setFieldError(passwordField, passwordError, "Password required");
                valid = false;
            } else if (!isValidPassword(password)) {
                setFieldError(passwordField, passwordError, "Min 8 chars with upper, lower, digit & special");
                valid = false;
            }
        } else {
            // In edit mode, password is optional. If provided, validate it.
            if (!password.isEmpty() && !isValidPassword(password)) {
                setFieldError(passwordField, passwordError, "Min 8 chars with upper, lower, digit & special");
                valid = false;
            }
        }
        // Validate first name.
        if (firstNameField.getText().trim().isEmpty()) {
            setFieldError(firstNameField, firstNameError, "First name required");
            valid = false;
        }
        // Validate last name.
        if (lastNameField.getText().trim().isEmpty()) {
            setFieldError(lastNameField, lastNameError, "Last name required");
            valid = false;
        }
        // Validate email.
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            setFieldError(emailField, emailError, "Email required");
            valid = false;
        } else if (!isValidEmail(email)) {
            setFieldError(emailField, emailError, "Invalid email");
            valid = false;
        }
        // Validate birthday.
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
        // Validate phone number.
        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{8}")) {
            setFieldError(phoneField, phoneError, "8-digit number required");
            valid = false;
        }
        // Validate address.
        if (addressField.getText().trim().isEmpty()) {
            setFieldError(addressField, addressError, "Address required");
            valid = false;
        }
        // Validate permis type.
        if (permisTypeComboBox.getValue() == null) {
            setFieldError(permisTypeComboBox, permisTypeError, "Select permis type");
            valid = false;
        }
        // Validate salaire.
        String salaireStr = salaireField.getText().trim();
        double salaire = 0.0;
        if (salaireStr.isEmpty()) {
            setFieldError(salaireField, salaireError, "Salaire required");
            valid = false;
        } else {
            try {
                salaire = Double.parseDouble(salaireStr);
                if (salaire <= 0) {
                    setFieldError(salaireField, salaireError, "Salaire must be positive");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(salaireField, salaireError, "Invalid salary number");
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        if (isEditMode) {
            // ---------- Update existing moniteur ----------
            // Update profile fields
            editingProfile.setNom(firstNameField.getText().trim());
            editingProfile.setPrenom(lastNameField.getText().trim());
            editingProfile.setEmail(email);
            editingProfile.setBirthday(birthdayPicker.getValue());
            try {
                editingProfile.setTel(Integer.parseInt(phone));
            } catch (NumberFormatException e) {
                editingProfile.setTel(0);
            }
            editingProfile.setAddresse(addressField.getText().trim());
            boolean profileUpdated = profileService.createOrUpdateProfile(editingProfile, null);
            if (!profileUpdated) {
                setFieldError(emailField, emailError, "Profile update error");
                return;
            }
            // Optionally update the user password if provided (implementation depends on your UserService)
            if (!password.isEmpty()) {
                boolean passwordUpdated = userService.updateUserPassword(editingProfile.getUserId(), password);
                if (!passwordUpdated) {
                    setFieldError(passwordField, passwordError, "Password update error");
                    return;
                }
            }
            // Update moniteur fields
            try {
                editingMoniteur.setPermisType(PermisType.valueOf(permisTypeComboBox.getValue()));
            } catch (IllegalArgumentException e) {
                setFieldError(permisTypeComboBox, permisTypeError, "Invalid permis type");
                return;
            }
            editingMoniteur.setSalaire(salaire);
            boolean moniteurUpdated = moniteurService.updateMoniteur(editingMoniteur, editingProfile);
            if (!moniteurUpdated) {
                showInlineError("Moniteur update error");
                return;
            }
            showSuccessNotification("Moniteur mis à jour avec succès !");
            clearForm();
        } else {
            // ---------- Create new moniteur ----------
            String username = usernameField.getText().trim();
            boolean userCreated = userService.createUser(username, password, "moniteur");
            if (!userCreated) {
                setFieldError(usernameField, usernameError, "Username might exist");
                return;
            }
            int userId = userService.getUserIdByUsername(username);
            if (userId == -1) {
                setFieldError(usernameField, usernameError, "User retrieval error");
                return;
            }
            // Create profile.
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
            // Create moniteur record.
            Moniteur moniteur = new Moniteur();
            moniteur.setUserId(userId);
            try {
                moniteur.setPermisType(PermisType.valueOf(permisTypeComboBox.getValue()));
            } catch (IllegalArgumentException e) {
                setFieldError(permisTypeComboBox, permisTypeError, "Invalid permis type");
                return;
            }
            moniteur.setSalaire(salaire);
            boolean moniteurCreated = moniteurService.createMoniteur(moniteur);
            if (!moniteurCreated) {
                showInlineError("Moniteur creation error");
                return;
            }
            showSuccessNotification("Moniteur ajouté avec succès !");
            clearForm();
        }
    }

    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationType.SUCCESS);
        }
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
        salaireField.clear();
        clearAllErrors();
        // Reset edit mode if applicable.
        isEditMode = false;
        editingMoniteur = null;
        editingProfile = null;
        btnSubmit.setText("Ajouter Moniteur");
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
        salaireField.getStyleClass().remove("error");

        usernameError.setText("");
        passwordError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        birthdayError.setText("");
        phoneError.setText("");
        addressError.setText("");
        permisTypeError.setText("");
        salaireError.setText("");
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

    private void showInlineError(String message) {
        System.err.println(message);
    }

    private boolean isValidPassword(String password) {
        // Symbol check is done using [^\\w\\s], which matches any non-alphanumeric, non-whitespace character.
        // This pattern enforces min length 8, plus at least 1 uppercase, 1 lowercase, 1 digit, and 1 symbol.
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$";
        return Pattern.matches(pattern, password);
    }


    private boolean isValidEmail(String email) {
        String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(pattern, email);
    }
}
