package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.dto.AuthRequest;
import com.hiresmart.dto.RegisterRequest;
import com.hiresmart.model.Role;
import com.hiresmart.ui.SceneManager;
import com.hiresmart.util.AnimationUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private StackPane rootPane;
    @FXML private StackPane bgStack;
    @FXML private ImageView bgImageView1;
    @FXML private ImageView bgImageView2;
    @FXML private Region bgOverlay;

    @FXML private HBox loginCard;
    @FXML private VBox authLeftPane;
    @FXML private StackPane authRightPane;
    @FXML private ImageView heroImageView;
    @FXML private Button socialGoogleBtn;
    @FXML private Button socialFacebookBtn;

    @FXML private Label authTitle;
    @FXML private Label authSubtitle;

    @FXML private Button roleStudentBtn;
    @FXML private Button roleRecruiterBtn;
    @FXML private Button roleAdminBtn;

    @FXML private HBox modeSegmentBox;
    @FXML private Button tabSignIn;
    @FXML private Button tabSignUp;
    @FXML private VBox adminNoticeBox;

    @FXML private VBox registerFields;
    @FXML private TextField fullNameField;
    @FXML private VBox companyBox;
    @FXML private TextField companyField;
    @FXML private TextField phoneField;

    @FXML private Label emailLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;

    @FXML private CheckBox rememberCheck;
    @FXML private Button demoPrefillBtn;
    @FXML private Label errorMessage;
    @FXML private Button submitButton;
    @FXML private ProgressIndicator loadingIndicator;

    private Role currentRole = Role.CANDIDATE;
    private boolean isRegisterMode = false;
    private boolean isPasswordVisible = false;

    // HD Background Slideshow state (if enabled)
    private final List<Image> bgImages = new ArrayList<>();
    private int currentImageIndex = 0;
    private Timeline bgSlideshowTimeline;

    @FXML
    public void initialize() {
        // 1. Password field bidirectional synchronization
        if (passwordField != null && passwordTextField != null) {
            passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        }

        // 2. Setup hero visuals (professional woman portrait & glass card)
        setupHeroVisuals();

        // 3. Setup optional HD background slideshow if elements are present
        if (bgImageView1 != null && bgImageView2 != null) {
            setupBackgroundSlideshow();
        }

        // 4. Entrance animation for the centered split card
        setupCardEntranceAnimation();

        // 5. Setup Responsive Card Sizing on screen shift/window resize
        setupResponsiveCardSizing();

        // 6. Default Role selection
        selectStudentRole();
    }

    private void setupHeroVisuals() {
        if (heroImageView != null) {
            try (InputStream is = getClass().getResourceAsStream("/images/login_person.jpg")) {
                if (is != null) {
                    heroImageView.setImage(new Image(is));
                    Rectangle clip = new Rectangle(280, 320);
                    clip.setArcWidth(24);
                    clip.setArcHeight(24);
                    heroImageView.setClip(clip);
                }
            } catch (Exception e) {
                log.warn("Could not load login hero image: {}", e.getMessage());
            }
        }
    }

    private void setupBackgroundSlideshow() {
        String[] imagePaths = {"/images/bg1.jpg", "/images/bg2.jpg", "/images/bg3.jpg"};
        for (String path : imagePaths) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    bgImages.add(new Image(is));
                }
            } catch (Exception e) {
                log.warn("Could not load background image {}: {}", path, e.getMessage());
            }
        }

        if (bgImageView1 != null && rootPane != null) {
            bgImageView1.fitWidthProperty().bind(rootPane.widthProperty());
            bgImageView1.fitHeightProperty().bind(rootPane.heightProperty());
        }
        if (bgImageView2 != null && rootPane != null) {
            bgImageView2.fitWidthProperty().bind(rootPane.widthProperty());
            bgImageView2.fitHeightProperty().bind(rootPane.heightProperty());
        }

        if (!bgImages.isEmpty()) {
            bgImageView1.setImage(bgImages.get(0));
            currentImageIndex = 0;

            // Cycle every 6 seconds with smooth cross-fade animation
            bgSlideshowTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> triggerBackgroundCrossFade()));
            bgSlideshowTimeline.setCycleCount(Animation.INDEFINITE);
            bgSlideshowTimeline.play();
        }
    }

    private void triggerBackgroundCrossFade() {
        if (bgImages.size() <= 1) return;

        int nextIndex = (currentImageIndex + 1) % bgImages.size();
        Image nextImg = bgImages.get(nextIndex);

        bgImageView2.setImage(nextImg);
        bgImageView2.setOpacity(0.0);

        FadeTransition fadeInNext = new FadeTransition(Duration.millis(1200), bgImageView2);
        fadeInNext.setFromValue(0.0);
        fadeInNext.setToValue(1.0);

        FadeTransition fadeOutCurrent = new FadeTransition(Duration.millis(1200), bgImageView1);
        fadeOutCurrent.setFromValue(1.0);
        fadeOutCurrent.setToValue(0.0);

        ParallelTransition crossFade = new ParallelTransition(fadeInNext, fadeOutCurrent);
        crossFade.setOnFinished(e -> {
            bgImageView1.setImage(nextImg);
            bgImageView1.setOpacity(1.0);
            bgImageView2.setOpacity(0.0);
            currentImageIndex = nextIndex;
        });
        crossFade.play();
    }

    private void setupCardEntranceAnimation() {
        if (loginCard == null) return;

        loginCard.setOpacity(0.0);
        loginCard.setScaleX(0.93);
        loginCard.setScaleY(0.93);

        FadeTransition ft = new FadeTransition(Duration.millis(500), loginCard);
        ft.setToValue(1.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(500), loginCard);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(ft, st);
        entrance.setDelay(Duration.millis(150));
        entrance.play();
    }

    private void setupResponsiveCardSizing() {
        if (rootPane == null || loginCard == null) return;

        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> adjustCardLayout(newVal.doubleValue(), rootPane.getHeight()));
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> adjustCardLayout(rootPane.getWidth(), newVal.doubleValue()));

        Platform.runLater(() -> adjustCardLayout(rootPane.getWidth(), rootPane.getHeight()));
    }

    private void adjustCardLayout(double winWidth, double winHeight) {
        if (loginCard == null || winWidth <= 0 || winHeight <= 0) return;

        // Dynamically compute split card width responding to screen width shifting
        double targetWidth = Math.min(940, Math.max(740, winWidth * 0.78));
        loginCard.setPrefWidth(targetWidth);
    }

    // ==================== ROLE SELECTION HANDLERS ====================

    @FXML
    public void selectStudentRole() {
        currentRole = Role.CANDIDATE;
        updateRoleButtonsActive(roleStudentBtn);

        // Show mode segment
        modeSegmentBox.setVisible(true);
        modeSegmentBox.setManaged(true);
        adminNoticeBox.setVisible(false);
        adminNoticeBox.setManaged(false);

        tabSignUp.setText("Create Student Account");
        emailField.setPromptText("candidate@hiresmart.com");

        if (emailField.getText() == null || emailField.getText().isBlank() ||
                "recruiter@hiresmart.com".equals(emailField.getText()) || "admin@hiresmart.com".equals(emailField.getText())) {
            emailField.setText("candidate@hiresmart.com");
            passwordField.setText("password123");
        }

        if (companyBox != null) {
            companyBox.setVisible(false);
            companyBox.setManaged(false);
        }

        updateTitlesAndLabels();
        hideError();
    }

    @FXML
    public void selectRecruiterRole() {
        currentRole = Role.RECRUITER;
        updateRoleButtonsActive(roleRecruiterBtn);

        // Show mode segment
        modeSegmentBox.setVisible(true);
        modeSegmentBox.setManaged(true);
        adminNoticeBox.setVisible(false);
        adminNoticeBox.setManaged(false);

        tabSignUp.setText("Create Recruiter Account");
        emailField.setPromptText("recruiter@company.com");

        if (emailField.getText() == null || emailField.getText().isBlank() ||
                "candidate@hiresmart.com".equals(emailField.getText()) || "admin@hiresmart.com".equals(emailField.getText())) {
            emailField.setText("recruiter@hiresmart.com");
            passwordField.setText("password123");
        }

        if (companyBox != null) {
            companyBox.setVisible(isRegisterMode);
            companyBox.setManaged(isRegisterMode);
        }

        updateTitlesAndLabels();
        hideError();
    }

    @FXML
    public void selectAdminRole() {
        currentRole = Role.ADMIN;
        updateRoleButtonsActive(roleAdminBtn);

        // Force Login Mode (No Registration for Admin)
        if (isRegisterMode) {
            switchModeToLogin();
        }

        modeSegmentBox.setVisible(false);
        modeSegmentBox.setManaged(false);
        adminNoticeBox.setVisible(true);
        adminNoticeBox.setManaged(true);

        emailField.setPromptText("admin@hiresmart.com");
        if (emailField.getText() == null || emailField.getText().isBlank() ||
                "candidate@hiresmart.com".equals(emailField.getText()) || "recruiter@hiresmart.com".equals(emailField.getText())) {
            emailField.setText("admin@hiresmart.com");
            passwordField.setText("admin123");
        }

        if (companyBox != null) {
            companyBox.setVisible(false);
            companyBox.setManaged(false);
        }

        updateTitlesAndLabels();
        hideError();
    }

    private void updateRoleButtonsActive(Button activeButton) {
        Button[] buttons = {roleStudentBtn, roleRecruiterBtn, roleAdminBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("role-select-active");
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("role-select-active")) {
            activeButton.getStyleClass().add("role-select-active");
        }
    }

    private void updateTitlesAndLabels() {
        if (isRegisterMode) {
            authTitle.setText("REGISTER");
            if (currentRole == Role.RECRUITER) {
                authSubtitle.setText("Create your organization recruiter account.");
                submitButton.setText("Register Recruiter Account");
            } else {
                authSubtitle.setText("Join HireSmart ATS for AI-driven job matching.");
                submitButton.setText("Register Candidate Account");
            }
        } else {
            authTitle.setText("LOGIN");
            if (currentRole == Role.ADMIN) {
                authSubtitle.setText("Protected access for system owner and ATS control.");
                submitButton.setText("Sign In as Administrator");
            } else if (currentRole == Role.RECRUITER) {
                authSubtitle.setText("Access your candidate pipelines & AI assessments.");
                submitButton.setText("Login Now");
            } else {
                authSubtitle.setText("How do I get started? Enter your credentials.");
                submitButton.setText("Login Now");
            }
        }
    }

    // ==================== MODE SWITCHING (LOGIN / REGISTER) ====================

    @FXML
    public void switchModeToLogin() {
        isRegisterMode = false;
        tabSignIn.getStyleClass().removeAll("mode-tab-active");
        tabSignIn.getStyleClass().add("mode-tab-active");
        tabSignUp.getStyleClass().remove("mode-tab-active");

        registerFields.setVisible(false);
        registerFields.setManaged(false);
        if (companyBox != null) {
            companyBox.setVisible(false);
            companyBox.setManaged(false);
        }

        if (emailField.getText() == null || emailField.getText().isBlank()) {
            fillDemoForCurrentRole();
        }

        updateTitlesAndLabels();
        hideError();
        AnimationUtils.fadeIn(loginCard, 200);
    }

    @FXML
    public void switchModeToRegister() {
        if (currentRole == Role.ADMIN) {
            showError("Administrator accounts cannot be created via public registration.");
            return;
        }

        isRegisterMode = true;
        tabSignUp.getStyleClass().removeAll("mode-tab-active");
        tabSignUp.getStyleClass().add("mode-tab-active");
        tabSignIn.getStyleClass().remove("mode-tab-active");

        registerFields.setVisible(true);
        registerFields.setManaged(true);
        if (companyBox != null) {
            boolean isRecruiter = currentRole == Role.RECRUITER;
            companyBox.setVisible(isRecruiter);
            companyBox.setManaged(isRecruiter);
        }

        // Clear prefilled demo credentials when entering register mode so users can enter new account details
        String currentEmail = emailField.getText() != null ? emailField.getText().trim() : "";
        List<String> demoEmails = List.of("candidate@hiresmart.com", "recruiter@hiresmart.com", "admin@hiresmart.com", "");
        if (demoEmails.contains(currentEmail)) {
            emailField.clear();
            passwordField.clear();
            if (passwordTextField != null) passwordTextField.clear();
            if (fullNameField != null) fullNameField.clear();
        }

        updateTitlesAndLabels();
        hideError();
        AnimationUtils.fadeIn(registerFields, 250);
    }

    // ==================== PASSWORD TOGGLE & DEMO FILL ====================

    @FXML
    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordBtn.setText("🔒");
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    public void fillDemoForCurrentRole() {
        switchModeToLogin();
        if (currentRole == Role.ADMIN) {
            emailField.setText("admin@hiresmart.com");
            passwordField.setText("admin123");
            passwordTextField.setText("admin123");
        } else if (currentRole == Role.RECRUITER) {
            emailField.setText("recruiter@hiresmart.com");
            passwordField.setText("password123");
            passwordTextField.setText("password123");
        } else {
            emailField.setText("candidate@hiresmart.com");
            passwordField.setText("password123");
            passwordTextField.setText("password123");
        }
        hideError();
        AnimationUtils.pulse(submitButton);
    }

    // ==================== SOCIAL 1-CLICK AUTHENTICATION ====================

    @FXML
    public void onGoogleLogin() {
        // Quick 1-click Candidate Demo Login (Google)
        selectStudentRole();
        emailField.setText("candidate@hiresmart.com");
        passwordField.setText("password123");
        if (passwordTextField != null) passwordTextField.setText("password123");
        onSubmit();
    }

    @FXML
    public void onFacebookLogin() {
        // Quick 1-click Recruiter Demo Login (Facebook)
        selectRecruiterRole();
        emailField.setText("recruiter@hiresmart.com");
        passwordField.setText("password123");
        if (passwordTextField != null) passwordTextField.setText("password123");
        onSubmit();
    }

    // ==================== SUBMISSION & AUTHENTICATION ====================

    @FXML
    public void onSubmit() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = "";
        if (isPasswordVisible && passwordTextField != null && passwordTextField.getText() != null) {
            password = passwordTextField.getText().trim();
        } else if (passwordField != null && passwordField.getText() != null) {
            password = passwordField.getText().trim();
        }

        if (email.isBlank()) {
            showError("Email address is required.");
            return;
        }
        if (password.isBlank()) {
            showError("Password is required.");
            return;
        }

        setLoading(true);

        if (isRegisterMode) {
            if (currentRole == Role.ADMIN) {
                showError("Admin accounts cannot be created via public registration.");
                setLoading(false);
                return;
            }

            String fullName = fullNameField.getText() != null ? fullNameField.getText().trim() : "";
            if (fullName.isBlank()) {
                showError("Full Name is required.");
                setLoading(false);
                return;
            }

            String companyName = "";
            if (currentRole == Role.RECRUITER) {
                companyName = companyField != null && companyField.getText() != null ? companyField.getText().trim() : "";
                if (companyName.isBlank()) {
                    showError("Company / Organization Name is required for Recruiter registration.");
                    setLoading(false);
                    return;
                }
            }

            RegisterRequest req = new RegisterRequest();
            req.setEmail(email);
            req.setPassword(password);
            req.setFullName(fullName);
            req.setRole(currentRole.name());
            req.setCompanyName(companyName);
            req.setPhone(phoneField.getText() != null ? phoneField.getText().trim() : "");

            ApiClient.getInstance().registerAsync(req, res -> {
                setLoading(false);
                if (res.isSuccess()) {
                    SceneManager.showMainLayout();
                } else {
                    showError(res.getMessage());
                }
            }, err -> {
                setLoading(false);
                showError(extractErrorMessage(err));
            });

        } else {
            // Auto-align role with demo account if user typed/filled standard demo credentials
            Role effectiveRole = currentRole;
            String cleanEmail = email.toLowerCase().trim();
            if ("admin@hiresmart.com".equals(cleanEmail) || "admin".equals(cleanEmail)) {
                effectiveRole = Role.ADMIN;
            } else if ("recruiter@hiresmart.com".equals(cleanEmail) || "recruiter@company.com".equals(cleanEmail) || "recruiter".equals(cleanEmail)) {
                effectiveRole = Role.RECRUITER;
            } else if ("candidate@hiresmart.com".equals(cleanEmail) || "student@university.edu".equals(cleanEmail) || "student@hiresmart.com".equals(cleanEmail) || "candidate".equals(cleanEmail)) {
                effectiveRole = Role.CANDIDATE;
            }

            // Include expected role to enforce backend role verification
            AuthRequest req = new AuthRequest(email, password, effectiveRole.name());
            ApiClient.getInstance().loginAsync(req, res -> {
                setLoading(false);
                if (res.isSuccess()) {
                    SceneManager.showMainLayout();
                } else {
                    showError(res.getMessage());
                }
            }, err -> {
                setLoading(false);
                showError(extractErrorMessage(err));
            });
        }
    }

    private String extractErrorMessage(Throwable err) {
        if (err == null) return "An unknown error occurred.";
        Throwable cause = err;
        while (cause.getCause() != null && cause != cause.getCause()) {
            if (cause instanceof org.springframework.web.server.ResponseStatusException rse) {
                if (rse.getReason() != null && !rse.getReason().isBlank()) {
                    return rse.getReason();
                }
            }
            cause = cause.getCause();
        }
        if (cause instanceof org.springframework.web.server.ResponseStatusException rse) {
            if (rse.getReason() != null && !rse.getReason().isBlank()) {
                return rse.getReason();
            }
        }
        String msg = cause.getMessage();
        if (msg != null && msg.contains("\"")) {
            int firstQuote = msg.indexOf('"');
            int lastQuote = msg.lastIndexOf('"');
            if (firstQuote >= 0 && lastQuote > firstQuote) {
                return msg.substring(firstQuote + 1, lastQuote);
            }
        }
        return msg != null ? msg : "Authentication failed.";
    }

    private void showError(String msg) {
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
        errorMessage.setManaged(true);
        AnimationUtils.shake(loginCard);
    }

    private void hideError() {
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        submitButton.setDisable(loading);
    }
}