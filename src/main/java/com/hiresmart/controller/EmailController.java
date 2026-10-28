package com.hiresmart.controller;

import com.hiresmart.client.UserSession;
import com.hiresmart.model.Notification;
import com.hiresmart.service.EmailNotificationService;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class EmailController {

    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, Long> colId;
    @FXML private TableColumn<Notification, String> colRecipient;
    @FXML private TableColumn<Notification, String> colSubject;
    @FXML private TableColumn<Notification, String> colType;
    @FXML private TableColumn<Notification, String> colStatus;
    @FXML private TableColumn<Notification, String> colTime;

    @FXML private Label previewToLabel;
    @FXML private Label previewSubjectLabel;
    @FXML private TextArea previewBodyText;

    private final EmailNotificationService emailService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EmailController(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(n -> new SimpleObjectProperty<>(n.getValue().getId()));
        colRecipient.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getRecipientEmail()));
        colSubject.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getSubject()));
        colType.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getType()));
        colStatus.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getStatus()));
        colTime.setCellValueFactory(n -> new SimpleStringProperty(
                n.getValue().getCreatedAt() != null ? n.getValue().getCreatedAt().format(DATE_FMT) : ""));

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

        // Listen for row selection to show preview
        notificationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                previewToLabel.setText(newVal.getRecipientName() != null ? newVal.getRecipientName() + " <" + newVal.getRecipientEmail() + ">" : newVal.getRecipientEmail());
                previewSubjectLabel.setText(newVal.getSubject());
                previewBodyText.setText(newVal.getBody());
            }
        });

        onRefreshLogs();
    }

    @FXML
    public void onRefreshLogs() {
        UserSession session = UserSession.getInstance();
        List<Notification> list;
        if (session.isCandidate()) {
            list = emailService.getNotificationsForUser(session.getEmail());
        } else {
            list = emailService.getAllNotifications();
        }

        notificationsTable.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) {
            notificationsTable.getSelectionModel().selectFirst();
        }
    }
}
