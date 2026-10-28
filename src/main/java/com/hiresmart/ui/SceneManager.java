package com.hiresmart.ui;

import com.hiresmart.util.AnimationUtils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Objects;

public class SceneManager {
    private static final Logger log = LoggerFactory.getLogger(SceneManager.class);
    private static Stage primaryStage;
    private static ConfigurableApplicationContext springContext;
    private static final String CSS = "/css/style.css";
    private static StackPane contentHolder;

    public static void init(Stage stage, ConfigurableApplicationContext context) {
        primaryStage = stage;
        springContext = context;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void registerContentHolder(StackPane holder) {
        contentHolder = holder;
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(Objects.requireNonNull(SceneManager.class.getResource(CSS)).toExternalForm());
            } catch (Exception ex) {
                log.warn("Could not load css: {}", ex.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("HireSmart ATS — AI-Powered Interview & Resume Screening");
            primaryStage.setWidth(1060);
            primaryStage.setHeight(720);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.centerOnScreen();
            primaryStage.show();

            AnimationUtils.fadeIn(root, 400);
        } catch (IOException e) {
            log.error("Failed to load login.fxml: {}", e.getMessage(), e);
        }
    }

    public static void showMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/main_layout.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(Objects.requireNonNull(SceneManager.class.getResource(CSS)).toExternalForm());
            } catch (Exception ex) {
                log.warn("Could not load css: {}", ex.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("HireSmart ATS — AI-Powered Recruitment Management Platform");
            primaryStage.setWidth(1280);
            primaryStage.setHeight(840);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(720);
            primaryStage.centerOnScreen();
            primaryStage.show();

            AnimationUtils.fadeIn(root, 350);
        } catch (IOException e) {
            log.error("Failed to load main_layout.fxml: {}", e.getMessage(), e);
        }
    }

    public static Node loadPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Node node = loader.load();
            return node;
        } catch (Exception e) {
            log.error("Failed to load pane from {}: {}", fxmlPath, e.getMessage(), e);
            return null;
        }
    }

    public static void navigateTo(String fxmlPath) {
        if (contentHolder == null) {
            Node pane = loadPane(fxmlPath);
            setCenterNode(pane);
            return;
        }

        Node node = loadPane(fxmlPath);
        if (node != null) {
            contentHolder.getChildren().setAll(node);
            AnimationUtils.slideInUp(node, 20, 300);
        }
    }

    public static void setCenterNode(Node node) {
        if (node == null) return;
        if (contentHolder != null) {
            contentHolder.getChildren().setAll(node);
            AnimationUtils.fadeIn(node, 250);
            return;
        }

        // Fallback: search open stage for #contentHolder
        if (primaryStage != null && primaryStage.getScene() != null) {
            Node holder = primaryStage.getScene().lookup("#contentHolder");
            if (holder instanceof StackPane sp) {
                contentHolder = sp;
                sp.getChildren().setAll(node);
                AnimationUtils.fadeIn(node, 250);
            }
        }
    }
}