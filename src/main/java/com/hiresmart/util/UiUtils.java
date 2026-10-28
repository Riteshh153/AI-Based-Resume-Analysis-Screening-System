package com.hiresmart.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;

public class UiUtils {

    public static void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Error Encountered");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static String getStatusBadgeStyle(String status) {
        if (status == null) return "badge-default";
        return switch (status.toUpperCase()) {
            case "SHORTLISTED", "SELECTED", "ACTIVE" -> "badge-success";
            case "INTERVIEW_SCHEDULED", "SCHEDULED" -> "badge-info";
            case "UNDER_REVIEW", "APPLIED", "PENDING" -> "badge-warning";
            case "REJECTED", "CANCELLED", "CLOSED" -> "badge-danger";
            default -> "badge-default";
        };
    }
}
