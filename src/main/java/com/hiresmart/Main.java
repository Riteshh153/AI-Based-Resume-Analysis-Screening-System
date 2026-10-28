package com.hiresmart;

import org.springframework.boot.SpringApplication;

public class Main {
    public static void main(String[] args) {
        for (String arg : args) {
            if ("--server".equalsIgnoreCase(arg) || "-server".equalsIgnoreCase(arg) || "--headless".equalsIgnoreCase(arg)) {
                System.out.println("==================================================================");
                System.out.println("[HireSmart ATS] Launching in Dedicated REST API Server Mode...");
                System.out.println("==================================================================");
                SpringApplication.run(AppConfig.class, args);
                return;
            }
        }
        // Default: Launch JavaFX Desktop GUI Application
        MainApp.main(args);
    }
}
