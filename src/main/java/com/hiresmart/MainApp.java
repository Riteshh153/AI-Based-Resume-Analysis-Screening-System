package com.hiresmart;

import com.hiresmart.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {
    private ConfigurableApplicationContext springContext;
    private static ConfigurableApplicationContext context;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(AppConfig.class)
                .headless(false)
                .run();
        MainApp.context = springContext;
    }

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage, springContext);
        SceneManager.showLogin();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return context;
    }

    public static void main(String[] args) {
        launch(args);
    }
}