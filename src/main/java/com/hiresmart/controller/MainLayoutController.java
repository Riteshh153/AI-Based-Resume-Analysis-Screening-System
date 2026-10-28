package com.hiresmart.controller;

import com.hiresmart.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

@Component
public class MainLayoutController {

    @FXML private StackPane contentHolder;

    @FXML
    public void initialize() {
        if (contentHolder != null) {
            SceneManager.registerContentHolder(contentHolder);
            // Default view: dashboard
            SceneManager.navigateTo("/fxml/dashboard.fxml");
        }
    }

    public void setContent(Node node) {
        if (contentHolder != null && node != null) {
            contentHolder.getChildren().setAll(node);
        }
    }
}