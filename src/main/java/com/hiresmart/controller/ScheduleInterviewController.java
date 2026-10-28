package com.hiresmart.controller;

import com.hiresmart.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class ScheduleInterviewController {

    @FXML private TextField candidateInfoInput;
    @FXML private TextField jobTitleInput;
    @FXML private ComboBox<String> roundCombo;
    @FXML private TextField interviewerInput;

    @FXML
    public void initialize() {
        if (roundCombo != null) {
            roundCombo.setItems(FXCollections.observableArrayList("TECHNICAL", "HR", "MANAGERIAL"));
            roundCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void onGoToPipeline() {
        SceneManager.navigateTo("/fxml/Interviews.fxml");
    }
}
