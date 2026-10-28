package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.model.Candidate;
import com.hiresmart.service.CandidateService;
import com.hiresmart.ui.SceneManager;
import com.hiresmart.util.AnimationUtils;
import com.hiresmart.util.UiUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DashboardController {

    @FXML private HBox welcomeBanner;
    @FXML private Label welcomeGreeting;
    @FXML private Label welcomeRoleBadge;

    @FXML private GridPane cardsGrid;
    @FXML private VBox card1;
    @FXML private VBox card2;
    @FXML private VBox card3;
    @FXML private VBox card4;

    @FXML private Label totalCandidates;
    @FXML private Label resumesScreened;
    @FXML private Label interviewsScheduled;
    @FXML private Label selectedCandidates;

    @FXML private TableView<Candidate> recentTable;
    @FXML private TableColumn<Candidate, String> colName;
    @FXML private TableColumn<Candidate, String> colEmail;
    @FXML private TableColumn<Candidate, String> colPosition;
    @FXML private TableColumn<Candidate, Integer> colScore;
    @FXML private TableColumn<Candidate, String> colStatus;
    @FXML private TableColumn<Candidate, String> colDate;

    private final CandidateService candidateService;

    public DashboardController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @FXML
    public void initialize() {
        // Table cell factories
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colPosition.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAppliedPosition()));
        colScore.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAiScore()).asObject());
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAppliedDate()));

        // Styled Status Badges in table
        colStatus.setCellFactory(column -> new TableCell<Candidate, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add(UiUtils.getStatusBadgeStyle(status));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // ATS Score cell styling
        colScore.setCellFactory(column -> new TableCell<Candidate, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else {
                    setText(score + "%");
                    if (score >= 85) {
                        setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    } else if (score >= 70) {
                        setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Set personalized greetings
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            welcomeGreeting.setText("Welcome back, " + session.getDisplayName() + "!");
            welcomeRoleBadge.setText(session.getRole().name());
        }

        // Animated entrances
        AnimationUtils.slideInUp(welcomeBanner, 20, 300);
        AnimationUtils.fadeIn(cardsGrid, 400);

        loadDashboardData();
    }

    private void loadDashboardData() {
        ApiClient.getInstance().getDashboardStatsAsync(stats -> {
            AnimationUtils.animateCounter(totalCandidates, stats.getTotalCandidates(), 600);
            AnimationUtils.animateCounter(resumesScreened, stats.getResumesScreened(), 600);
            AnimationUtils.animateCounter(interviewsScheduled, stats.getScheduledInterviews(), 600);
            AnimationUtils.animateCounter(selectedCandidates, stats.getSelectedCandidates(), 600);
        }, err -> {
            // Fallback to candidateService if API background fails
            totalCandidates.setText(String.valueOf(candidateService.countAll()));
            resumesScreened.setText(String.valueOf(candidateService.countScreened()));
            interviewsScheduled.setText(String.valueOf(candidateService.countInterviews()));
            selectedCandidates.setText(String.valueOf(candidateService.countSelected()));
        });

        // Load candidates asynchronously
        Platform.runLater(() -> {
            List<Candidate> recent = candidateService.findRecent(10);
            recentTable.setItems(FXCollections.observableArrayList(recent));
        });
    }

    @FXML
    public void onQuickAction1() {
        SceneManager.navigateTo("/fxml/ResumeScreening.fxml");
    }

    @FXML
    public void onQuickAction2() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Master Recruitment Excel Report");
        fileChooser.setInitialFileName("HireSmart_Master_Recruitment_Report.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        File file = fileChooser.showSaveDialog(SceneManager.getPrimaryStage());

        if (file != null) {
            ApiClient.getInstance().exportReportAsync("master", file, savedFile -> {
                UiUtils.showInfo("Export Successful", "Master Excel report generated successfully:\n" + savedFile.getAbsolutePath());
            }, err -> {
                UiUtils.showError("Export Failed", "Error creating Excel file: " + err.getMessage());
            });
        }
    }

    @FXML
    public void onViewAllCandidates() {
        SceneManager.navigateTo("/fxml/Candidates.fxml");
    }
}