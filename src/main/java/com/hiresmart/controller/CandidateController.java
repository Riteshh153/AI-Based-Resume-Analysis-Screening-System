package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.model.Application;
import com.hiresmart.model.Role;
import com.hiresmart.service.ApplicationService;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CandidateController {

    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterReview;
    @FXML private Button btnFilterShortlisted;
    @FXML private Button btnFilterInterviews;
    @FXML private Button btnFilterSelected;

    @FXML private VBox statusUpdatePane;
    @FXML private Label statusModalTitle;
    @FXML private ComboBox<String> statusSelector;
    @FXML private TextField statusReasonInput;
    @FXML private TextArea statusNotesInput;

    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, Long> colAppId;
    @FXML private TableColumn<Application, String> colCandidateName;
    @FXML private TableColumn<Application, String> colCandidateEmail;
    @FXML private TableColumn<Application, String> colJobTitle;
    @FXML private TableColumn<Application, Integer> colMatchScore;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TableColumn<Application, String> colAppliedAt;
    @FXML private TableColumn<Application, Void> colAction;

    private final ApplicationService applicationService;
    private List<Application> fullList = new ArrayList<>();
    private Application selectedApp;

    public CandidateController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @FXML
    public void initialize() {
        if (statusSelector != null) {
            statusSelector.setItems(FXCollections.observableArrayList(
                    "UNDER_REVIEW", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED", "WITHDRAWN"
            ));
            statusSelector.getSelectionModel().selectFirst();
        }

        colAppId.setCellValueFactory(a -> new SimpleObjectProperty<>(a.getValue().getId()));
        colCandidateName.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getCandidateName()));
        colCandidateEmail.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getCandidateEmail()));
        colJobTitle.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getJobTitle() + " (" + a.getValue().getCompanyName() + ")"));
        colMatchScore.setCellValueFactory(a -> new SimpleIntegerProperty(a.getValue().getMatchScore()).asObject());
        colStatus.setCellValueFactory(a -> new SimpleStringProperty(a.getValue().getStatus()));
        colAppliedAt.setCellValueFactory(a -> new SimpleStringProperty(
                a.getValue().getAppliedAt() != null ? a.getValue().getAppliedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""));

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

        // Score coloring
        colMatchScore.setCellFactory(col -> new TableCell<>() {
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

        setupActionButtons();
        loadApplications();
    }

    private void setupActionButtons() {
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Update Status");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Application app = getTableView().getItems().get(getIndex());
                    Role role = UserSession.getInstance().getRole();
                    if (role == Role.CANDIDATE) {
                        btn.setText("View Details");
                        btn.getStyleClass().setAll("secondary-button");
                        btn.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                        btn.setOnAction(e -> UiUtils.showInfo("Application Details",
                                "Target Role: " + app.getJobTitle() + "\nCurrent Status: " + app.getStatus() +
                                "\nRecruiter Notes: " + app.getRecruiterNotes()));
                    } else {
                        btn.setText("Update Status");
                        btn.getStyleClass().setAll("secondary-button");
                        btn.setStyle("-fx-padding: 4 8; -fx-font-size: 11px;");
                        btn.setOnAction(e -> openStatusModal(app));
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void openStatusModal(Application app) {
        this.selectedApp = app;
        statusModalTitle.setText("Update Status: " + app.getCandidateName() + " (" + app.getJobTitle() + ")");
        statusSelector.setValue(app.getStatus());
        statusReasonInput.setText("Progressed following screening review.");
        statusNotesInput.setText(app.getRecruiterNotes() != null ? app.getRecruiterNotes() : "");

        statusUpdatePane.setVisible(true);
        statusUpdatePane.setManaged(true);
        AnimationUtils.slideInUp(statusUpdatePane, 20, 300);
    }

    @FXML
    public void onCloseStatusModal() {
        statusUpdatePane.setVisible(false);
        statusUpdatePane.setManaged(false);
    }

    @FXML
    public void onSubmitStatusUpdate() {
        if (selectedApp == null) return;

        String newStatus = statusSelector.getValue();
        String reason = statusReasonInput.getText();
        String notes = statusNotesInput.getText();

        ApiClient.getInstance().updateApplicationStatusAsync(
                selectedApp.getId(), newStatus, reason, notes,
                ok -> {
                    onCloseStatusModal();
                    loadApplications();
                    UiUtils.showInfo("Status Updated", "Application stage changed to: " + newStatus + "\nApplicant notified via email.");
                },
                err -> UiUtils.showError("Update Error", err.getMessage())
        );
    }

    private void loadApplications() {
        UserSession session = UserSession.getInstance();
        if (session.isCandidate() && session.getProfile() != null) {
            ApiClient.getInstance().getApplicationsByCandidateAsync(session.getProfile().getId(), list -> {
                this.fullList = list;
                applicationsTable.setItems(FXCollections.observableArrayList(list));
            }, err -> {});
        } else {
            ApiClient.getInstance().getAllApplicationsAsync(list -> {
                this.fullList = list;
                applicationsTable.setItems(FXCollections.observableArrayList(list));
            }, err -> {});
        }
    }

    @FXML public void filterAll() { applicationsTable.setItems(FXCollections.observableArrayList(fullList)); }
    @FXML public void filterReview() { filterByStatus("UNDER_REVIEW"); }
    @FXML public void filterShortlisted() { filterByStatus("SHORTLISTED"); }
    @FXML public void filterInterviews() { filterByStatus("INTERVIEW_SCHEDULED"); }
    @FXML public void filterSelected() { filterByStatus("SELECTED"); }

    private void filterByStatus(String status) {
        List<Application> filtered = fullList.stream().filter(a -> status.equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        applicationsTable.setItems(FXCollections.observableArrayList(filtered));
    }
}
