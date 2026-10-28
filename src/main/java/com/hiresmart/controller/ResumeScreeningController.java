package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.dao.JobDao;
import com.hiresmart.dao.ResumeDao;
import com.hiresmart.dto.ScreeningResponse;
import com.hiresmart.model.Job;
import com.hiresmart.model.Resume;
import com.hiresmart.model.ScreeningResult;
import com.hiresmart.service.ScreeningService;
import com.hiresmart.util.AnimationUtils;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ResumeScreeningController {

    @FXML private ComboBox<Resume> resumeSelector;
    @FXML private ComboBox<Job> jobSelector;
    @FXML private Button btnAnalyzeScreening;

    @FXML private VBox resultsDashboardPane;
    @FXML private Label overallScoreLabel;
    @FXML private Label engineBadgeLabel;
    @FXML private Label skillScoreLabel;
    @FXML private ProgressBar skillProgressBar;
    @FXML private Label expScoreLabel;
    @FXML private ProgressBar expProgressBar;
    @FXML private Label atsScoreLabel;
    @FXML private ProgressBar atsProgressBar;

    @FXML private FlowPane matchedSkillsPane;
    @FXML private FlowPane missingSkillsPane;
    @FXML private VBox recommendationsListPane;

    @FXML private TableView<ScreeningResult> historyTable;
    @FXML private TableColumn<ScreeningResult, Long> colHistId;
    @FXML private TableColumn<ScreeningResult, String> colHistCandidate;
    @FXML private TableColumn<ScreeningResult, String> colHistJob;
    @FXML private TableColumn<ScreeningResult, Integer> colHistScore;
    @FXML private TableColumn<ScreeningResult, Integer> colHistAts;
    @FXML private TableColumn<ScreeningResult, String> colHistEngine;
    @FXML private TableColumn<ScreeningResult, String> colHistDate;

    private final ScreeningService screeningService;
    private final ResumeDao resumeDao;
    private final JobDao jobDao;

    public ResumeScreeningController(ScreeningService screeningService, ResumeDao resumeDao, JobDao jobDao) {
        this.screeningService = screeningService;
        this.resumeDao = resumeDao;
        this.jobDao = jobDao;
    }

    @FXML
    public void initialize() {
        configureSelectors();
        configureTable();
        loadHistory();
    }

    private void configureSelectors() {
        List<Resume> resumes = resumeDao.findByCandidateId(1L);
        if (resumes.isEmpty()) {
            Resume mockR = new Resume();
            mockR.setId(1L);
            mockR.setFileName("Sample_Candidate_Resume.pdf");
            resumes = List.of(mockR);
        }
        resumeSelector.setItems(FXCollections.observableArrayList(resumes));
        resumeSelector.setConverter(new StringConverter<>() {
            @Override public String toString(Resume r) { return r != null ? "Resume #" + r.getId() + " — " + r.getFileName() : ""; }
            @Override public Resume fromString(String string) { return null; }
        });
        if (!resumes.isEmpty()) resumeSelector.getSelectionModel().selectFirst();

        List<Job> jobs = jobDao.findAll();
        jobSelector.setItems(FXCollections.observableArrayList(jobs));
        jobSelector.setConverter(new StringConverter<>() {
            @Override public String toString(Job j) { return j != null ? j.getTitle() + " (" + j.getCompanyName() + ")" : ""; }
            @Override public Job fromString(String string) { return null; }
        });
        if (!jobs.isEmpty()) jobSelector.getSelectionModel().selectFirst();
    }

    private void configureTable() {
        colHistId.setCellValueFactory(r -> new SimpleObjectProperty<>(r.getValue().getId()));
        colHistCandidate.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getCandidateName() != null ? r.getValue().getCandidateName() : "Candidate"));
        colHistJob.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getJobTitle() != null ? r.getValue().getJobTitle() : "Software Engineer"));
        colHistScore.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getOverallMatchScore()).asObject());
        colHistAts.setCellValueFactory(r -> new SimpleIntegerProperty(r.getValue().getAtsCompatibilityScore()).asObject());
        colHistEngine.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getEngineType()));
        colHistDate.setCellValueFactory(r -> new SimpleStringProperty(
                r.getValue().getScreenedAt() != null ? r.getValue().getScreenedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));

        colHistScore.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else {
                    setText(score + "%");
                    if (score >= 85) setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadHistory() {
        ApiClient.getInstance().getAllScreeningResultsAsync(list -> {
            historyTable.setItems(FXCollections.observableArrayList(list));
        }, err -> {});
    }

    @FXML
    public void onRunScreening() {
        Resume selectedResume = resumeSelector.getValue();
        Job selectedJob = jobSelector.getValue();

        if (selectedResume == null || selectedJob == null) {
            UiUtils.showError("Selection Required", "Please select both a candidate resume and a job requisition.");
            return;
        }

        btnAnalyzeScreening.setText("Analyzing...");
        btnAnalyzeScreening.setDisable(true);

        ApiClient.getInstance().screenResumeAsync(selectedResume.getId(), selectedJob.getId(), response -> {
            btnAnalyzeScreening.setText("⚡ Run ATS Screening");
            btnAnalyzeScreening.setDisable(false);

            if (response.isSuccess()) {
                displayScreeningResults(response);
                loadHistory();
            } else {
                UiUtils.showError("Screening Error", response.getMessage());
            }
        }, err -> {
            btnAnalyzeScreening.setText("⚡ Run ATS Screening");
            btnAnalyzeScreening.setDisable(false);
            UiUtils.showError("Error", err.getMessage());
        });
    }

    private void displayScreeningResults(ScreeningResponse res) {
        ScreeningResult sr = res.getResult();

        overallScoreLabel.setText(sr.getOverallMatchScore() + "%");
        engineBadgeLabel.setText(sr.getEngineType());

        skillScoreLabel.setText(sr.getSkillMatchScore() + "%");
        skillProgressBar.setProgress(sr.getSkillMatchScore() / 100.0);

        expScoreLabel.setText(sr.getExperienceMatchScore() + "%");
        expProgressBar.setProgress(sr.getExperienceMatchScore() / 100.0);

        atsScoreLabel.setText(sr.getAtsCompatibilityScore() + "%");
        atsProgressBar.setProgress(sr.getAtsCompatibilityScore() / 100.0);

        // Matched Skills
        matchedSkillsPane.getChildren().clear();
        if (res.getMatchedSkillsList() != null) {
            for (String skill : res.getMatchedSkillsList()) {
                Label chip = new Label("✓ " + skill);
                chip.getStyleClass().add("skill-chip");
                matchedSkillsPane.getChildren().add(chip);
            }
        }

        // Missing Skills
        missingSkillsPane.getChildren().clear();
        if (res.getMissingSkillsList() != null && !res.getMissingSkillsList().isEmpty()) {
            for (String skill : res.getMissingSkillsList()) {
                Label chip = new Label("✕ " + skill);
                chip.getStyleClass().add("skill-chip-missing");
                missingSkillsPane.getChildren().add(chip);
            }
        } else {
            Label chip = new Label("✓ All required competencies covered");
            chip.getStyleClass().add("skill-chip");
            missingSkillsPane.getChildren().add(chip);
        }

        // Recommendations
        recommendationsListPane.getChildren().clear();
        if (res.getRecommendationsList() != null) {
            for (String rec : res.getRecommendationsList()) {
                Label recLabel = new Label("• " + rec);
                recLabel.setWrapText(true);
                recLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
                recommendationsListPane.getChildren().add(recLabel);
            }
        }

        resultsDashboardPane.setVisible(true);
        resultsDashboardPane.setManaged(true);
        AnimationUtils.scaleIn(resultsDashboardPane, 350);
    }
}
