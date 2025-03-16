package controller;

import entite.DossierCandidat;
import entite.Profile;
import service.DossierCandidatService;
import service.PaymentInstallmentService;
import service.PaymentService;
import service.ProfileService;
import service.UserService;
import service.AutoEcoleService;
import Utils.NotificationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.regex.Pattern;

public class AddCandidateController {

    // Root container – must be a StackPane for notifications to overlay properly
    @FXML private StackPane rootPane;

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

    // Dossier candidat fields
    @FXML private ComboBox<String> permisTypeComboBox;
    @FXML private Button btnChooseCIN;
    @FXML private Button btnChooseCertificat;
    @FXML private Button btnChoosePhoto;

    // New fields for session counts and payment mode
    @FXML private TextField seancesConduiteField;
    @FXML private Label seancesConduiteError;
    @FXML private TextField seancesCodeField;
    @FXML private Label seancesCodeError;
    @FXML private ComboBox<String> modePaiementComboBox;
    @FXML private Label modePaiementError;

    @FXML private Button btnSubmit;

    // Error labels for existing fields
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
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    // Add the missing autoEcoleService
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @FXML
    public void initialize() {
        // Initialize permis type drop-down
        permisTypeComboBox.getItems().addAll("A", "B", "C");
        // Initialize payment mode options
        modePaiementComboBox.getItems().addAll("comptant", "par facilités");

        // Clear error listeners
        addClearErrorListener(usernameField, usernameError);
        addClearErrorListener(passwordField, passwordError);
        addClearErrorListener(firstNameField, firstNameError);
        addClearErrorListener(lastNameField, lastNameError);
        addClearErrorListener(emailField, emailError);
        addClearErrorListener(seancesConduiteField, seancesConduiteError);
        addClearErrorListener(seancesCodeField, seancesCodeError);
        modePaiementComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            modePaiementComboBox.getStyleClass().remove("error");
            modePaiementError.setText("");
        });
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

        // File chooser handlers
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
        // Validate new fields: session counts and payment mode
        int seancesConduite = 0;
        try {
            seancesConduite = Integer.parseInt(seancesConduiteField.getText().trim());
            if (seancesConduite < 0) {
                setFieldError(seancesConduiteField, seancesConduiteError, "Must be non-negative");
                valid = false;
            }
        } catch (NumberFormatException e) {
            setFieldError(seancesConduiteField, seancesConduiteError, "Invalid number");
            valid = false;
        }
        int seancesCode = 0;
        try {
            seancesCode = Integer.parseInt(seancesCodeField.getText().trim());
            if (seancesCode < 0) {
                setFieldError(seancesCodeField, seancesCodeError, "Must be non-negative");
                valid = false;
            }
        } catch (NumberFormatException e) {
            setFieldError(seancesCodeField, seancesCodeError, "Invalid number");
            valid = false;
        }
        if (modePaiementComboBox.getValue() == null) {
            setFieldError(modePaiementComboBox, modePaiementError, "Select payment mode");
            valid = false;
        }
        if (!valid) {
            return;
        }

        // Create candidate user
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
        // Create candidate profile
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
        // Create candidate dossier
        DossierCandidat dossier = new DossierCandidat();
        dossier.setCandidateId(userId);
        dossier.setPermisType(permisTypeComboBox.getValue());
        LocalDateTime now = LocalDateTime.now();
        dossier.setCreatedAt(now);
        dossier.setUpdatedAt(now);
        dossier.setNombreSeancesConduite(seancesConduite);
        dossier.setNombreSeancesCode(seancesCode);
        boolean dossierCreated = dossierService.createDossier(dossier, cinFile, certificatFile, photoFile);
        if (!dossierCreated) {
            showInlineError("Dossier creation error");
            return;
        }

        // Payment processing:
        // Calculate total amount based on auto-école configuration pricing.
        double totalAmount = 0;
        try {
            // Retrieve auto-école data. Assume first record contains pricing info at indices 4 and 5.
            String[][] autoEcoleData = autoEcoleService.getAutoEcoleData().toArray(new String[0][]);
            double prixSeanceConduit = Double.parseDouble(autoEcoleData[0][4]);
            double prixSeanceCode = Double.parseDouble(autoEcoleData[0][5]);
            totalAmount = (seancesConduite * prixSeanceConduit) + (seancesCode * prixSeanceCode);
        } catch (Exception e) {
            e.printStackTrace();
            showInlineError("Error retrieving auto-école pricing configuration.");
            return;
        }

        String modePaiement = modePaiementComboBox.getValue();
        if ("comptant".equalsIgnoreCase(modePaiement)) {
            boolean paymentCreated = paymentService.createFullPayment(userId, totalAmount, LocalDate.now());
            if (!paymentCreated) {
                showInlineError("Payment creation error");
                return;
            }
        } else if ("par facilités".equalsIgnoreCase(modePaiement)) {
            // Create installment payment entry.
            Optional<entite.Payment> optPayment = paymentService.createInstallmentPayment(userId, totalAmount, LocalDate.now());
            if (optPayment.isPresent()) {
                entite.Payment payment = optPayment.get();
                // Create 12 installment entries (one per month)
                double installmentAmount = totalAmount / 12;
                LocalDate startDate = LocalDate.now();
                for (int i = 1; i <= 12; i++) {
                    LocalDate dueDate = startDate.plusMonths(i);
                    entite.PaymentInstallment installment = new entite.PaymentInstallment(
                            payment.getId(),
                            i,
                            dueDate,
                            installmentAmount,
                            entite.PaymentInstallment.Status.PENDING
                    );
                    boolean installmentCreated = installmentService.createInstallment(installment);
                    if (!installmentCreated) {
                        showInlineError("Failed to create installment " + i);
                    }
                }
            } else {
                showInlineError("Installment payment creation error");
                return;
            }
        }

        // Show success notification
        NotificationUtil.showNotification(rootPane, "Candidate added successfully!", NotificationUtil.NotificationType.SUCCESS);
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
        seancesConduiteField.clear();
        seancesCodeField.clear();
        modePaiementComboBox.getSelectionModel().clearSelection();
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
        seancesConduiteField.getStyleClass().remove("error");
        seancesCodeField.getStyleClass().remove("error");
        modePaiementComboBox.getStyleClass().remove("error");

        usernameError.setText("");
        passwordError.setText("");
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        birthdayError.setText("");
        phoneError.setText("");
        addressError.setText("");
        permisTypeError.setText("");
        seancesConduiteError.setText("");
        seancesCodeError.setText("");
        modePaiementError.setText("");
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

    private void showInlineError(String message) {
        System.err.println(message);
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,}$";
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
