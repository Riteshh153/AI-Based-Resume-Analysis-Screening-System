package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.ui.SceneManager;
import com.hiresmart.util.UiUtils;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ReportController {

    private void handleExport(String type, String defaultFilename) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save " + defaultFilename);
        fc.setInitialFileName(defaultFilename);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel (*.xlsx)", "*.xlsx"));
        File file = fc.showSaveDialog(SceneManager.getPrimaryStage());

        if (file != null) {
            ApiClient.getInstance().exportReportAsync(type, file, saved -> {
                UiUtils.showInfo("Export Successful", "Report generated successfully using Apache POI:\n" + saved.getAbsolutePath());
            }, err -> {
                UiUtils.showError("Export Error", err.getMessage());
            });
        }
    }

    @FXML public void onExportMaster() { handleExport("master", "HireSmart_Master_Recruitment_Report.xlsx"); }
    @FXML public void onExportCandidates() { handleExport("candidates", "HireSmart_Candidate_Directory.xlsx"); }
    @FXML public void onExportJobs() { handleExport("jobs", "HireSmart_Job_Requisitions.xlsx"); }
    @FXML public void onExportApplications() { handleExport("applications", "HireSmart_Application_Funnel.xlsx"); }
    @FXML public void onExportScreening() { handleExport("screening", "HireSmart_ATS_Screening_Results.xlsx"); }
    @FXML public void onExportInterviews() { handleExport("interviews", "HireSmart_Interviews_And_Scorecards.xlsx"); }
}
