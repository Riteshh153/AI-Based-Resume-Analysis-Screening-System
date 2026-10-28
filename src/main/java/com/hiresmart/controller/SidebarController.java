package com.hiresmart.controller;

import com.hiresmart.client.UserSession;
import com.hiresmart.model.Role;
import com.hiresmart.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SidebarController {

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnJobs;
    @FXML private Button btnScreening;
    @FXML private Button btnApplications;
    @FXML private Button btnInterviews;
    @FXML private Button btnMockInterview;
    @FXML private Button btnReports;
    @FXML private Button btnNotifications;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        configureRoleVisibility();
    }

    private void configureRoleVisibility() {
        Role role = UserSession.getInstance().getRole();
        if (role == Role.CANDIDATE) {
            btnProfile.setVisible(true);
            btnProfile.setManaged(true);
            btnMockInterview.setVisible(true);
            btnMockInterview.setManaged(true);
            btnReports.setVisible(false);
            btnReports.setManaged(false);
            btnJobs.setText("💼   Browse Jobs");
            btnApplications.setText("📋   My Applications");
        } else if (role == Role.RECRUITER) {
            btnProfile.setVisible(false);
            btnProfile.setManaged(false);
            btnMockInterview.setVisible(false);
            btnMockInterview.setManaged(false);
            btnReports.setVisible(true);
            btnReports.setManaged(true);
            btnJobs.setText("💼   Manage Jobs");
            btnApplications.setText("📋   Applicant Pipeline");
        } else { // ADMIN
            btnProfile.setVisible(false);
            btnProfile.setManaged(false);
            btnMockInterview.setVisible(false);
            btnMockInterview.setManaged(false);
            btnReports.setVisible(true);
            btnReports.setManaged(true);
            btnJobs.setText("💼   Job Management");
            btnApplications.setText("📋   All Applications");
        }
    }

    private void setActive(Button activeBtn) {
        List<Button> buttons = List.of(
                btnDashboard, btnProfile, btnJobs, btnScreening,
                btnApplications, btnInterviews, btnMockInterview,
                btnReports, btnNotifications, btnSettings
        );
        for (Button b : buttons) {
            if (b != null) {
                b.getStyleClass().remove("sidebar-button-active");
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("sidebar-button-active")) {
            activeBtn.getStyleClass().add("sidebar-button-active");
        }
    }

    @FXML
    public void goDashboard() {
        setActive(btnDashboard);
        SceneManager.navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    public void goProfile() {
        setActive(btnProfile);
        SceneManager.navigateTo("/fxml/CandidateProfile.fxml");
    }

    @FXML
    public void goJobs() {
        setActive(btnJobs);
        SceneManager.navigateTo("/fxml/JobPositions.fxml");
    }

    @FXML
    public void goScreening() {
        setActive(btnScreening);
        SceneManager.navigateTo("/fxml/ResumeScreening.fxml");
    }

    @FXML
    public void goApplications() {
        setActive(btnApplications);
        SceneManager.navigateTo("/fxml/Candidates.fxml");
    }

    @FXML
    public void goInterviews() {
        setActive(btnInterviews);
        SceneManager.navigateTo("/fxml/Interviews.fxml");
    }

    @FXML
    public void goMockInterview() {
        setActive(btnMockInterview);
        SceneManager.navigateTo("/fxml/AddCandidate.fxml");
    }

    @FXML
    public void goReports() {
        setActive(btnReports);
        SceneManager.navigateTo("/fxml/Reports.fxml");
    }

    @FXML
    public void goEmail() {
        setActive(btnNotifications);
        SceneManager.navigateTo("/fxml/Email.fxml");
    }

    @FXML
    public void goSettings() {
        setActive(btnSettings);
        SceneManager.navigateTo("/fxml/Settings.fxml");
    }

    @FXML
    public void onLogout() {
        UserSession.getInstance().clear();
        SceneManager.showLogin();
    }
}