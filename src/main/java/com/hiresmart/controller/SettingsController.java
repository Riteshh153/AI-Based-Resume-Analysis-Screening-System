package com.hiresmart.controller;

import com.hiresmart.client.UserSession;
import com.hiresmart.dao.AuditLogDao;
import com.hiresmart.model.AuditLog;
import com.hiresmart.ui.SceneManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SettingsController {

    @FXML private Label dbStatus;
    @FXML private Label accountName;
    @FXML private Label accountEmail;
    @FXML private Label accountRole;

    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, Long> colLogId;
    @FXML private TableColumn<AuditLog, String> colLogUser;
    @FXML private TableColumn<AuditLog, String> colLogAction;
    @FXML private TableColumn<AuditLog, String> colLogEntity;
    @FXML private TableColumn<AuditLog, String> colLogDetails;
    @FXML private TableColumn<AuditLog, String> colLogTime;

    private final AuditLogDao auditLogDao;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SettingsController(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            accountName.setText(session.getDisplayName());
            accountEmail.setText(session.getEmail());
            accountRole.setText(session.getRole().name());
        }

        colLogId.setCellValueFactory(l -> new SimpleObjectProperty<>(l.getValue().getId()));
        colLogUser.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getUserEmail()));
        colLogAction.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getAction()));
        colLogEntity.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getEntityType()));
        colLogDetails.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getDetails()));
        colLogTime.setCellValueFactory(l -> new SimpleStringProperty(
                l.getValue().getCreatedAt() != null ? l.getValue().getCreatedAt().format(DATE_FMT) : ""));

        loadAuditLogs();
    }

    private void loadAuditLogs() {
        List<AuditLog> logs = auditLogDao.findRecent(25);
        auditTable.setItems(FXCollections.observableArrayList(logs));
    }

    @FXML
    public void onSignOut() {
        UserSession.getInstance().clear();
        SceneManager.showLogin();
    }
}
