package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.dao.ApplicationDao;
import com.hiresmart.dto.InterviewFeedbackRequest;
import com.hiresmart.dto.InterviewScheduleRequest;
import com.hiresmart.model.Application;
import com.hiresmart.model.Interview;
import com.hiresmart.model.Role;
import com.hiresmart.service.InterviewService;
import com.hiresmart.util.AnimationUtils;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class InterviewController {

    @FXML private Button btnOpenScheduleModal;

    @FXML private VBox scheduleModalPane;
    @FXML private ComboBox<Application> applicationSelector;
    @FXML private ComboBox<String> roundTypeSelector;
    @FXML private TextField interviewerNameInput;
    @FXML private TextField interviewerEmailInput;
    @FXML private TextField meetingLinkInput;
    @FXML private TextField scheduleTimeInput;

    @FXML private VBox evaluationModalPane;
    @FXML private Label evalModalTitle;
    @FXML private TextField evalTechInput;
    @FXML private TextField evalCommInput;
    @FXML private TextField evalProblemInput;
    @FXML private ComboBox<String> evalRecSelector;
    @FXML private TextArea evalNotesInput;

    @FXML private TableView<Interview> interviewsTable;
    @FXML private TableColumn<Interview, Long> colIvId;
    @FXML private TableColumn<Interview, String> colCandidate;
    @FXML private TableColumn<Interview, String> colJob;
    @FXML private TableColumn<Interview, String> colRound;
    @FXML private TableColumn<Interview, String> colTime;
    @FXML private TableColumn<Interview, String> colInterviewer;
    @FXML private TableColumn<Interview, String> colStatus;
    @FXML private TableColumn<Interview, Integer> colScore;
    @FXML private TableColumn<Interview, String> colRec;
    @FXML private TableColumn<Interview, Void> colAction;

    private final InterviewService interviewService;
    private final ApplicationDao applicationDao;
    private Interview selectedInterview;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InterviewController(InterviewService interviewService, ApplicationDao applicationDao) {
        this.interviewService = interviewService;
        this.applicationDao = applicationDao;
    }

    @FXML
    public void initialize() {
        if (roundTypeSelector != null) {
            roundTypeSelector.setItems(FXCollections.observableArrayList("TECHNICAL", "HR", "MANAGERIAL", "SYSTEM_DESIGN"));
            roundTypeSelector.getSelectionModel().selectFirst();
        }
        if (evalRecSelector != null) {
            evalRecSelector.setItems(FXCollections.observableArrayList("STRONG_HIRE", "HIRE", "HOLD", "REJECT"));
            evalRecSelector.getSelectionModel().selectFirst();
        }

        colIvId.setCellValueFactory(i -> new SimpleObjectProperty<>(i.getValue().getId()));
        colCandidate.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getCandidateName()));
        colJob.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getJobTitle() + " (" + i.getValue().getCompanyName() + ")"));
        colRound.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getRoundType()));
        colTime.setCellValueFactory(i -> new SimpleStringProperty(
                i.getValue().getScheduledTime() != null ? i.getValue().getScheduledTime().format(DATE_FMT) : ""));
        colInterviewer.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getInterviewerName()));
        colStatus.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getStatus()));
        colScore.setCellValueFactory(i -> new SimpleIntegerProperty(i.getValue().getAverageScore()).asObject());
        colRec.setCellValueFactory(i -> new SimpleStringProperty(i.getValue().getRecommendation()));

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

        // Recommendation badge
        colRec.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String rec, boolean empty) {
                super.updateItem(rec, empty);
                if (empty || rec == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(rec);
                    badge.getStyleClass().add(UiUtils.getStatusBadgeStyle(rec));
                    setGraphic(badge);
                }
            }
        });

        setupActionButtons();
        configureRoleControls();
        loadInterviews();
        populateApplications();
    }

    private void configureRoleControls() {
        Role role = UserSession.getInstance().getRole();
        boolean canSchedule = (role == Role.RECRUITER || role == Role.ADMIN);
        if (btnOpenScheduleModal != null) {
            btnOpenScheduleModal.setVisible(canSchedule);
            btnOpenScheduleModal.setManaged(canSchedule);
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
                    Interview iv = getTableView().getItems().get(getIndex());
                    Role role = UserSession.getInstance().getRole();

                    if (role == Role.CANDIDATE) {
                        btn.setText("Join Meet");
                        btn.getStyleClass().setAll("secondary-button");
                        btn.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                        btn.setOnAction(e -> UiUtils.showInfo("Meeting Link", "Join video conference:\n" + iv.getMeetingLink()));
                    } else {
                        btn.setText("⭐ Evaluate");
                        btn.getStyleClass().setAll("secondary-button");
                        btn.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                        btn.setOnAction(e -> openEvaluationModal(iv));
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void populateApplications() {
        List<Application> apps = applicationDao.findAll();
        applicationSelector.setItems(FXCollections.observableArrayList(apps));
        applicationSelector.setConverter(new StringConverter<>() {
            @Override public String toString(Application a) { return a != null ? a.getCandidateName() + " — " + a.getJobTitle() : ""; }
            @Override public Application fromString(String string) { return null; }
        });
        if (!apps.isEmpty()) applicationSelector.getSelectionModel().selectFirst();
    }

    private void loadInterviews() {
        UserSession session = UserSession.getInstance();
        if (session.isCandidate() && session.getProfile() != null) {
            ApiClient.getInstance().getCandidateInterviewsAsync(session.getProfile().getId(), list -> {
                interviewsTable.setItems(FXCollections.observableArrayList(list));
            }, err -> {});
        } else {
            ApiClient.getInstance().getAllInterviewsAsync(list -> {
                interviewsTable.setItems(FXCollections.observableArrayList(list));
            }, err -> {});
        }
    }

    @FXML
    public void onOpenScheduleModal() {
        scheduleTimeInput.setText(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0).format(DATE_FMT));
        scheduleModalPane.setVisible(true);
        scheduleModalPane.setManaged(true);
        AnimationUtils.slideInUp(scheduleModalPane, 20, 300);
    }

    @FXML
    public void onCloseScheduleModal() {
        scheduleModalPane.setVisible(false);
        scheduleModalPane.setManaged(false);
    }

    @FXML
    public void onSubmitSchedule() {
        Application selectedApp = applicationSelector.getValue();
        if (selectedApp == null) {
            UiUtils.showError("Application Required", "Please select a candidate application.");
            return;
        }

        InterviewScheduleRequest req = new InterviewScheduleRequest();
        req.setApplicationId(selectedApp.getId());
        req.setCandidateId(selectedApp.getCandidateId());
        req.setJobId(selectedApp.getJobId());
        req.setRoundType(roundTypeSelector.getValue());
        req.setInterviewerName(interviewerNameInput.getText().trim());
        req.setInterviewerEmail(interviewerEmailInput.getText().trim());
        req.setMeetingLink(meetingLinkInput.getText().trim());
        try {
            req.setScheduledTime(LocalDateTime.parse(scheduleTimeInput.getText().trim(), DATE_FMT));
        } catch (Exception e) {
            req.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        }

        ApiClient.getInstance().scheduleInterviewAsync(req, iv -> {
            onCloseScheduleModal();
            loadInterviews();
            UiUtils.showInfo("Interview Scheduled", "Interview scheduled with " + iv.getInterviewerName() + "!\nInvitation email dispatched.");
        }, err -> {
            UiUtils.showError("Scheduling Failed", err.getMessage());
        });
    }

    private void openEvaluationModal(Interview iv) {
        this.selectedInterview = iv;
        evalModalTitle.setText("Evaluation Scorecard: " + iv.getCandidateName() + " (" + iv.getRoundType() + ")");
        evalTechInput.setText(String.valueOf(iv.getTechnicalScore() > 0 ? iv.getTechnicalScore() : 85));
        evalCommInput.setText(String.valueOf(iv.getCommunicationScore() > 0 ? iv.getCommunicationScore() : 80));
        evalProblemInput.setText(String.valueOf(iv.getProblemSolvingScore() > 0 ? iv.getProblemSolvingScore() : 90));
        evalRecSelector.setValue(iv.getRecommendation() != null && !iv.getRecommendation().equals("PENDING") ? iv.getRecommendation() : "STRONG_HIRE");
        evalNotesInput.setText(iv.getFeedbackNotes() != null ? iv.getFeedbackNotes() : "");

        evaluationModalPane.setVisible(true);
        evaluationModalPane.setManaged(true);
        AnimationUtils.slideInUp(evaluationModalPane, 20, 300);
    }

    @FXML
    public void onCloseEvalModal() {
        evaluationModalPane.setVisible(false);
        evaluationModalPane.setManaged(false);
    }

    @FXML
    public void onSubmitEvaluation() {
        if (selectedInterview == null) return;

        InterviewFeedbackRequest req = new InterviewFeedbackRequest();
        try {
            req.setTechnicalScore(Integer.parseInt(evalTechInput.getText().trim()));
            req.setCommunicationScore(Integer.parseInt(evalCommInput.getText().trim()));
            req.setProblemSolvingScore(Integer.parseInt(evalProblemInput.getText().trim()));
        } catch (Exception ignored) {
            req.setTechnicalScore(80);
            req.setCommunicationScore(80);
            req.setProblemSolvingScore(80);
        }
        req.setRecommendation(evalRecSelector.getValue());
        req.setFeedbackNotes(evalNotesInput.getText());
        req.setStatus("COMPLETED");

        ApiClient.getInstance().submitInterviewFeedbackAsync(selectedInterview.getId(), req, ok -> {
            onCloseEvalModal();
            loadInterviews();
            UiUtils.showInfo("Evaluation Saved", "Scorecard recorded. Candidate application progressed based on recommendation: " + req.getRecommendation());
        }, err -> {
            UiUtils.showError("Error", err.getMessage());
        });
    }
}
