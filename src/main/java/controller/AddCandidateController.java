package controller;

import entite.DossierCandidat;
import entite.Profile;
import entite.User;
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


    @FXML
    private StackPane rootPane;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private DatePicker birthdayPicker;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<String> permisTypeComboBox;
    @FXML
    private Button btnChooseCIN;
    @FXML
    private Button btnChooseCertificat;
    @FXML
    private Button btnChoosePhoto;

    @FXML
    private TextField seancesConduiteField;
    @FXML
    private Label seancesConduiteError;
    @FXML
    private TextField seancesCodeField;
    @FXML
    private Label seancesCodeError;
    @FXML
    private ComboBox<String> modePaiementComboBox;
    @FXML
    private Label modePaiementError;

    @FXML
    private Button btnSubmit;

    @FXML
    private Label usernameError;
    @FXML
    private Label passwordError;
    @FXML
    private Label firstNameError;
    @FXML
    private Label lastNameError;
    @FXML
    private Label emailError;
    @FXML
    private Label birthdayError;
    @FXML
    private Label phoneError;
    @FXML
    private Label addressError;
    @FXML
    private Label permisTypeError;
    @FXML
    private Label cinError;
    @FXML
    private Label certificatError;
    @FXML
    private Label photoError;

    private File cinFile;
    private File certificatFile;
    private File photoFile;

    private boolean isEditMode = false;
    private DossierCandidat editingCandidate;
    private Profile editingProfile;

    private final UserService userService = new UserService();
    private final ProfileService profileService = new ProfileService();
    private final DossierCandidatService dossierService = new DossierCandidatService();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @FXML
    public void initialize() {
        permisTypeComboBox.getItems().addAll("A", "B", "C");
        modePaiementComboBox.getItems().addAll("comptant", "par facilités");

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


    public void initData(DossierCandidat dossier, Profile profile) {
        isEditMode = true;
        editingCandidate = dossier;
        editingProfile = profile;

        User existingUser = userService.getUserById(profile.getUserId());
        if (existingUser != null) {
            usernameField.setText(existingUser.getUsername());
            usernameField.setDisable(true);
        }
        passwordField.clear();

        firstNameField.setText(profile.getNom());
        lastNameField.setText(profile.getPrenom());
        emailField.setText(profile.getEmail());
        birthdayPicker.setValue(profile.getBirthday());
        phoneField.setText(String.valueOf(profile.getTel()));
        addressField.setText(profile.getAddresse());


        permisTypeComboBox.setValue(dossier.getPermisType());

        seancesConduiteField.setText(String.valueOf(dossier.getNombreSeancesConduite()));
        seancesCodeField.setText(String.valueOf(dossier.getNombreSeancesCode()));
        modePaiementComboBox.setDisable(true);

        btnSubmit.setText("Mettre à jour Candidat");
    }

    private void handleSubmit(ActionEvent event) {
        clearAllErrors();
        boolean valid = true;

        if (usernameField.getText().trim().isEmpty()) {
            setFieldError(usernameField, usernameError, "Username required");
            valid = false;
        }
        String password = passwordField.getText().trim();
        if (!isEditMode) {
            if (password.isEmpty()) {
                setFieldError(passwordField, passwordError, "Password required");
                valid = false;
            } else if (!isValidPassword(password)) {
                setFieldError(passwordField, passwordError, "Min 8 chars with upper, lower, digit & special");
                valid = false;
            }
        } else {
            if (!password.isEmpty() && !isValidPassword(password)) {
                setFieldError(passwordField, passwordError, "Min 8 chars with upper, lower, digit & special");
                valid = false;
            }
        }
        if (firstNameField.getText().trim().isEmpty()) {
            setFieldError(firstNameField, firstNameError, "First name required");
            valid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            setFieldError(lastNameField, lastNameError, "Last name required");
            valid = false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            setFieldError(emailField, emailError, "Email required");
            valid = false;
        } else if (!isValidEmail(email)) {
            setFieldError(emailField, emailError, "Invalid email");
            valid = false;
        }
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
        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{8}")) {
            setFieldError(phoneField, phoneError, "8-digit number required");
            valid = false;
        }
        if (addressField.getText().trim().isEmpty()) {
            setFieldError(addressField, addressError, "Address required");
            valid = false;
        }
        if (permisTypeComboBox.getValue() == null) {
            setFieldError(permisTypeComboBox, permisTypeError, "Select permis type");
            valid = false;
        }
        if (!isEditMode) {
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
        }
        int seancesConduite = 0;
        try {
            seancesConduite = Integer.parseInt(seancesConduiteField.getText().trim());
            if (seancesConduite < 0) {
                setFieldError(seancesConduiteField, seancesConduiteError, "Must be non-negative");
                valid = false;
            } else if (seancesConduite < 10) {
                setFieldError(seancesConduiteField, seancesConduiteError, "Minimum 10 séances requises");
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
            } else if (seancesCode < 5) {
                setFieldError(seancesCodeField, seancesCodeError, "Minimum 5 séances requises");
                valid = false;
            }
        } catch (NumberFormatException e) {
            setFieldError(seancesCodeField, seancesCodeError, "Invalid number");
            valid = false;
        }

        if (!isEditMode && modePaiementComboBox.getValue() == null) {
            setFieldError(modePaiementComboBox, modePaiementError, "Select payment mode");
            valid = false;
        }
        if (!valid) {
            return;
        }

        if (isEditMode) {
            editingProfile.setNom(firstNameField.getText().trim());
            editingProfile.setPrenom(lastNameField.getText().trim());
            editingProfile.setEmail(emailField.getText().trim());
            editingProfile.setBirthday(birthdayPicker.getValue());
            try {
                editingProfile.setTel(Integer.parseInt(phoneField.getText().trim()));
            } catch (NumberFormatException e) {
                editingProfile.setTel(0);
            }
            editingProfile.setAddresse(addressField.getText().trim());
            boolean profileUpdated = profileService.createOrUpdateProfile(editingProfile, null);
            if (!profileUpdated) {
                setFieldError(emailField, emailError, "Profile update error");
                return;
            }
            if (!password.isEmpty()) {
                boolean passwordUpdated = userService.updateUserPassword(editingProfile.getUserId(), password);
                if (!passwordUpdated) {
                    setFieldError(passwordField, passwordError, "Password update error");
                    return;
                }
            }

            editingCandidate.setPermisType(permisTypeComboBox.getValue());
            editingCandidate.setNombreSeancesConduite(seancesConduite);
            editingCandidate.setNombreSeancesCode(seancesCode);

            boolean dossierUpdated = dossierService.updateDossier(editingCandidate,
                    (cinFile != null ? cinFile : null),
                    (certificatFile != null ? certificatFile : null),
                    (photoFile != null ? photoFile : null));
            if (!dossierUpdated) {
                showInlineError("Dossier update error");
                return;
            }
            showSuccessNotification("Candidat mis à jour avec succès !");
            clearForm();
        } else {
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
            dossier.setNombreSeancesConduite(seancesConduite);
            dossier.setNombreSeancesCode(seancesCode);
            boolean dossierCreated = dossierService.createDossier(dossier, cinFile, certificatFile, photoFile);
            if (!dossierCreated) {
                showInlineError("Dossier creation error");
                return;
            }


            double totalAmount = 0;
            try {

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
                Optional<entite.Payment> optPayment = paymentService.createInstallmentPayment(userId, totalAmount, LocalDate.now());
                if (optPayment.isPresent()) {
                    entite.Payment payment = optPayment.get();
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

            showSuccessNotification("Candidat ajouté avec succès !");
            clearForm();
        }
    }

    private void showSuccessNotification(String message) {
        StackPane contentArea = (StackPane) rootPane.getScene().lookup("#contentArea");
        if (contentArea != null) {
            NotificationUtil.showNotification(contentArea, message, NotificationUtil.NotificationType.SUCCESS);
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
        btnChooseCIN.setText("Choisir le fichier");
        btnChooseCertificat.setText("Choisir le fichier");
        btnChoosePhoto.setText("Choisir le fichier");
        cinFile = null;
        certificatFile = null;
        photoFile = null;
        seancesConduiteField.clear();
        seancesCodeField.clear();
        if (!isEditMode) {
            modePaiementComboBox.getSelectionModel().clearSelection();
        }
        clearAllErrors();

        isEditMode = false;
        editingCandidate = null;
        editingProfile = null;
        btnSubmit.setText("Ajouter Candidat");
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

        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$";
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
