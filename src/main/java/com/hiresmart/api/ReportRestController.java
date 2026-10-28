package com.hiresmart.api;

import com.hiresmart.service.ExcelReportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportRestController {
    private final ExcelReportService reportService;

    public ReportRestController(ExcelReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping({"/download/{reportType}", "/excel/{reportType}"})
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable String reportType) throws IOException {
        File tempFile = File.createTempFile("HireSmart_Report_", ".xlsx");

        switch (reportType.toLowerCase()) {
            case "candidates" -> reportService.exportCandidatesReport(tempFile);
            case "jobs" -> reportService.exportJobsReport(tempFile);
            case "applications" -> reportService.exportApplicationsReport(tempFile);
            case "screening" -> reportService.exportScreeningReport(tempFile);
            case "interviews" -> reportService.exportInterviewsReport(tempFile);
            default -> reportService.exportMasterRecruitmentReport(tempFile);
        }

        byte[] data = Files.readAllBytes(tempFile.toPath());
        tempFile.deleteOnExit();

        String filename = "HireSmart_" + capitalize(reportType) + "_Report.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Recruitment";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
