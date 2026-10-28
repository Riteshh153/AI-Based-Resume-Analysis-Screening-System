package com.hiresmart.controller;

import com.hiresmart.client.UserSession;
import com.hiresmart.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class TopBarController {

    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userRoleBadge;

    @FXML
    public void initialize() {
        refreshUserInfo();
    }

    public void refreshUserInfo() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            if (userNameLabel != null) userNameLabel.setText(session.getDisplayName());
            if (userEmailLabel != null) userEmailLabel.setText(session.getEmail());
            if (userRoleBadge != null) userRoleBadge.setText(session.getRole().name());
        }
    }

    public void setPageHeader(String title, String subtitle) {
        if (pageTitle != null) pageTitle.setText(title);
        if (pageSubtitle != null) pageSubtitle.setText(subtitle);
    }

    @FXML
    public void openNotifications() {
        SceneManager.navigateTo("/fxml/Email.fxml");
    }
}