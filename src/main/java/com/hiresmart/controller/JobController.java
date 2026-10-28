package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.dto.ApplicationRequest;
import com.hiresmart.model.Job;
import com.hiresmart.model.Resume;
import com.hiresmart.model.Role;
import com.hiresmart.service.JobService;
import com.hiresmart.service.ResumeService;
import com.hiresmart.util.AnimationUtils;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobController {

    @FXML private TextField searchKeywordInput;
    @FXML private TextField searchLocationInput;
    @FXML private Button btnOpenCreateJob;

    @FXML private VBox createJobFormPane;
    @FXML private TextField newJobTitle;
    @FXML private TextField newJobCompany;
    @FXML private TextField newJobDepartment;
    @FXML private TextField newJobLocation;
    @FXML private ComboBox<String> newJobType;
    @FXML private TextField newJobExperience;
    @FXML private TextField newJobSkills;
    @FXML private TextArea newJobDescription;

    @FXML private TableView<Job> jobsTable;
    @FXML private TableColumn<Job, Long> colJobId;
    @FXML private TableColumn<Job, String> colTitle;
    @FXML private TableColumn<Job, String> colCompany;
    @FXML private TableColumn<Job, String> colLocation;
    @FXML private TableColumn<Job, String> colType;
    @FXML private TableColumn<Job, Integer> colExp;
    @FXML private TableColumn<Job, String> colSkills;
    @FXML private TableColumn<Job, String> colStatus;
    @FXML private TableColumn<Job, Void> colAction;

    private final JobService jobService;
    private final ResumeService resumeService;

    public JobController(JobService jobService, ResumeService resumeService) {
        this.jobService = jobService;
        this.resumeService = resumeService;
    }

    @FXML
    public void initialize() {
        if (newJobType != null) {
            newJobType.setItems(FXCollections.observableArrayList("Full-time", "Part-time", "Contract", "Remote"));
            newJobType.getSelectionModel().selectFirst();
        }

        colJobId.setCellValueFactory(j -> new SimpleObjectProperty<>(j.getValue().getId()));
        colTitle.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getTitle()));
        colCompany.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getCompanyName()));
        colLocation.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getLocation()));
        colType.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getJobType()));
        colExp.setCellValueFactory(j -> new SimpleIntegerProperty(j.getValue().getExperienceRequired()).asObject());
        colSkills.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getRequiredSkills()));
        colStatus.setCellValueFactory(j -> new SimpleStringProperty(j.getValue().getStatus()));

        // Status badge
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add(UiUtils.getStatusBadgeStyle(status));
                    setGraphic(badge);
                }
            }
        });

        setupActionButtons();
        configureRoleControls();
        loadJobs();
    }

    private void configureRoleControls() {
        Role role = UserSession.getInstance().getRole();
        boolean canCreate = (role == Role.RECRUITER || role == Role.ADMIN);
        if (btnOpenCreateJob != null) {
            btnOpenCreateJob.setVisible(canCreate);
            btnOpenCreateJob.setManaged(canCreate);
        }
    }

    private void setupActionButtons() {
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Job job = getTableView().getItems().get(getIndex());
                    Role role = UserSession.getInstance().getRole();

                    if (role == Role.CANDIDATE) {
                        btn.setText("Apply Now");
                        btn.getStyleClass().setAll("primary-button");
                        btn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                        btn.setOnAction(e -> handleApplyJob(job));
                    } else {
                        btn.setText("Close Job");
                        btn.getStyleClass().setAll("danger-button");
                        btn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
                        btn.setOnAction(e -> handleCloseJob(job));
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void handleApplyJob(Job job) {
        UserSession session = UserSession.getInstance();
        if (session.getProfile() == null) {
            UiUtils.showError("Application Incomplete", "Please complete your candidate profile before submitting an application.");
            return;
        }

        Long candidateProfileId = session.getProfile().getId();
        var resumes = resumeService.getResumesByCandidateId(candidateProfileId);
        if (resumes.isEmpty()) {
            UiUtils.showError("No Resume Found", "Please upload a resume in 'My Profile' before applying for jobs.");
            return;
        }

        Resume activeResume = resumes.get(0);
        ApplicationRequest req = new ApplicationRequest(candidateProfileId, job.getId(), activeResume.getId(),
                "Candidate applied via HireSmart ATS Desktop Portal.");

        ApiClient.getInstance().applyAsync(req, app -> {
            UiUtils.showInfo("Application Submitted", "Your application for " + job.getTitle() + " at " + job.getCompanyName() + " has been submitted successfully!\nConfirmation email dispatched.");
        }, err -> {
            UiUtils.showError("Application Failed", err.getMessage());
        });
    }

    private void handleCloseJob(Job job) {
        boolean confirm = UiUtils.showConfirmation("Confirm Action", "Close Job Posting?", "Are you sure you want to close " + job.getTitle() + "?");
        if (confirm) {
            jobService.closeJob(job.getId(), UserSession.getInstance().getEmail());
            loadJobs();
            UiUtils.showInfo("Job Closed", "Job posting status changed to CLOSED.");
        }
    }

    private void loadJobs() {
        ApiClient.getInstance().getAllJobsAsync(list -> {
            jobsTable.setItems(FXCollections.observableArrayList(list));
        }, err -> {});
    }

    @FXML
    public void onSearchJobs() {
        String kw = searchKeywordInput.getText();
        String loc = searchLocationInput.getText();
        List<Job> results = jobService.searchJobs(kw, loc, null);
        jobsTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    public void onOpenCreateJob() {
        createJobFormPane.setVisible(true);
        createJobFormPane.setManaged(true);
        AnimationUtils.slideInUp(createJobFormPane, 20, 300);
    }

    @FXML
    public void onCloseCreateJobPane() {
        createJobFormPane.setVisible(false);
        createJobFormPane.setManaged(false);
    }

    @FXML
    public void onSubmitNewJob() {
        String title = newJobTitle.getText();
        String company = newJobCompany.getText();
        String location = newJobLocation.getText();
        String skills = newJobSkills.getText();
        String desc = newJobDescription.getText();

        if (title == null || title.isBlank() || company == null || company.isBlank() || skills == null || skills.isBlank()) {
            UiUtils.showError("Missing Fields", "Job Title, Company Name, and Required Skills are mandatory.");
            return;
        }

        Job job = new Job();
        Long recruiterId = UserSession.getInstance().getUser() != null ? UserSession.getInstance().getUser().getId() : 1L;
        job.setRecruiterId(recruiterId);
        job.setTitle(title.trim());
        job.setCompanyName(company.trim());
        job.setDepartment(newJobDepartment.getText());
        job.setLocation(location != null ? location.trim() : "Remote");
        job.setJobType(newJobType.getValue());
        try {
            job.setExperienceRequired(Integer.parseInt(newJobExperience.getText().trim()));
        } catch (Exception ignored) {
            job.setExperienceRequired(1);
        }
        job.setRequiredSkills(skills.trim());
        job.setDescription(desc != null ? desc.trim() : "Exciting job opportunity at " + company);
        job.setStatus("ACTIVE");

        ApiClient.getInstance().createJobAsync(job, id -> {
            onCloseCreateJobPane();
            loadJobs();
            UiUtils.showInfo("Job Published", "Job Requisition #" + id + " has been published successfully!");
        }, err -> {
            UiUtils.showError("Error", err.getMessage());
        });
    }
}
