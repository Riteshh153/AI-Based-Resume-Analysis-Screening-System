package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.model.CandidateProfile;
import com.hiresmart.model.Resume;
import com.hiresmart.service.CandidateService;
import com.hiresmart.ui.SceneManager;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CandidateProfileController {

    @FXML private Label headerNameLabel;
    @FXML private Label headerRoleBadge;
    @FXML private Label headerHeadlineLabel;
    @FXML private Label completionPercentageLabel;
    @FXML private ProgressBar completionProgressBar;

    @FXML private TextField fullNameInput;
    @FXML private TextField phoneInput;
    @FXML private TextField headlineInput;
    @FXML private TextField locationInput;
    @FXML private TextField qualificationInput;
    @FXML private TextField experienceYearsInput;
    @FXML private TextField skillsInput;
    @FXML private TextArea summaryInput;
    @FXML private TextField linkedinInput;
    @FXML private TextField githubInput;
    @FXML private Label profileStatusMessage;
    @FXML private Button btnSaveProfile;

    @FXML private Button btnUploadResume;
    @FXML private Label extractedSkillsLabel;
    @FXML private Label extractedEducationLabel;
    @FXML private Label extractedExperienceLabel;

    @FXML private TableView<Resume> resumeVersionTable;
    @FXML private TableColumn<Resume, Integer> colVersion;
    @FXML private TableColumn<Resume, String> colFileName;
    @FXML private TableColumn<Resume, String> colFileType;
    @FXML private TableColumn<Resume, String> colUploadDate;
    @FXML private TableColumn<Resume, String> colIsActive;

    private final CandidateService candidateService;
    private CandidateProfile currentProfile;

    public CandidateProfileController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @FXML
    public void initialize() {
        // Table columns
        colVersion.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getVersionNumber()).asObject());
        colFileName.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getFileName()));
        colFileType.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getFileType()));
        colUploadDate.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getUploadedAt() != null ? r.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""));
        colIsActive.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().isActive() ? "Active" : "Archived"));

        loadProfileData();
    }

    private void loadProfileData() {
        UserSession session = UserSession.getInstance();
        Long userId = session.getUser() != null ? session.getUser().getId() : 1L;

        candidateService.getProfileByUserId(userId).ifPresent(profile -> {
            this.currentProfile = profile;
            headerNameLabel.setText(profile.getFullName() != null ? profile.getFullName() : session.getDisplayName());
            headerHeadlineLabel.setText(profile.getHeadline() != null ? profile.getHeadline() : "Candidate Profile");

            fullNameInput.setText(profile.getFullName() != null ? profile.getFullName() : session.getDisplayName());
            phoneInput.setText(profile.getPhone() != null ? profile.getPhone() : "");
            headlineInput.setText(profile.getHeadline() != null ? profile.getHeadline() : "");
            locationInput.setText(profile.getLocation() != null ? profile.getLocation() : "");
            qualificationInput.setText(profile.getQualification() != null ? profile.getQualification() : "");
            experienceYearsInput.setText(String.valueOf(profile.getExperienceYears()));
            skillsInput.setText(profile.getSkills() != null ? profile.getSkills() : "");
            summaryInput.setText(profile.getExperienceSummary() != null ? profile.getExperienceSummary() : "");
            linkedinInput.setText(profile.getLinkedinUrl() != null ? profile.getLinkedinUrl() : "");
            githubInput.setText(profile.getGithubUrl() != null ? profile.getGithubUrl() : "");

            int comp = profile.calculateCompletion();
            completionPercentageLabel.setText(comp + "%");
            completionProgressBar.setProgress(comp / 100.0);

            loadResumes(profile.getId());
        });
    }

    private void loadResumes(Long candidateProfileId) {
        ApiClient.getInstance().getCandidateResumesAsync(candidateProfileId, list -> {
            resumeVersionTable.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                Resume active = list.get(0);
                if (active.getExtractedSkills() != null) extractedSkillsLabel.setText(active.getExtractedSkills());
                if (active.getExtractedEducation() != null) extractedEducationLabel.setText(active.getExtractedEducation());
                if (active.getExtractedExperience() != null) extractedExperienceLabel.setText(active.getExtractedExperience());
            }
        }, err -> {});
    }

    @FXML
    public void onSaveProfile() {
        if (currentProfile == null) return;

        currentProfile.setHeadline(headlineInput.getText());
        currentProfile.setLocation(locationInput.getText());
        currentProfile.setQualification(qualificationInput.getText());
        currentProfile.setSkills(skillsInput.getText());
        try {
            currentProfile.setExperienceYears(Integer.parseInt(experienceYearsInput.getText().trim()));
        } catch (NumberFormatException ignored) {}
        currentProfile.setExperienceSummary(summaryInput.getText());
        currentProfile.setLinkedinUrl(linkedinInput.getText());
        currentProfile.setGithubUrl(githubInput.getText());

        boolean ok = candidateService.updateProfile(currentProfile, fullNameInput.getText(), phoneInput.getText());
        if (ok) {
            int comp = currentProfile.calculateCompletion();
            completionPercentageLabel.setText(comp + "%");
            completionProgressBar.setProgress(comp / 100.0);
            headerHeadlineLabel.setText(currentProfile.getHeadline());
            profileStatusMessage.setText("✓ Profile saved successfully!");
        } else {
            profileStatusMessage.setText("Failed to save profile.");
        }
    }

    @FXML
    public void onUploadResume() {
        if (currentProfile == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Select Resume Document");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resume Files (*.pdf, *.docx)", "*.pdf", "*.docx"),
                new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents (*.docx)", "*.docx")
        );
        File file = fc.showOpenDialog(SceneManager.getPrimaryStage());

        if (file != null) {
            btnUploadResume.setText("Parsing...");
            btnUploadResume.setDisable(true);

            ApiClient.getInstance().uploadResumeAsync(currentProfile.getId(), file, file.getName(), resume -> {
                btnUploadResume.setText("⬆ Upload Resume");
                btnUploadResume.setDisable(false);

                if (resume.getExtractedSkills() != null) extractedSkillsLabel.setText(resume.getExtractedSkills());
                if (resume.getExtractedEducation() != null) extractedEducationLabel.setText(resume.getExtractedEducation());
                if (resume.getExtractedExperience() != null) extractedExperienceLabel.setText(resume.getExtractedExperience());

                loadResumes(currentProfile.getId());
                UiUtils.showInfo("Resume Parsed", "Resume v" + resume.getVersionNumber() + " uploaded and parsed successfully!");
            }, err -> {
                btnUploadResume.setText("⬆ Upload Resume");
                btnUploadResume.setDisable(false);
                UiUtils.showError("Upload Error", err.getMessage());
            });
        }
    }
}
