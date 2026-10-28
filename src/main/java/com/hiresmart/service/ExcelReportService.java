package com.hiresmart.service;

import com.hiresmart.dao.*;
import com.hiresmart.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportService {
    private static final Logger log = LoggerFactory.getLogger(ExcelReportService.class);

    private final CandidateProfileDao candidateDao;
    private final JobDao jobDao;
    private final ApplicationDao applicationDao;
    private final ScreeningResultDao screeningResultDao;
    private final InterviewDao interviewDao;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ExcelReportService(CandidateProfileDao candidateDao, JobDao jobDao,
                              ApplicationDao applicationDao, ScreeningResultDao screeningResultDao,
                              InterviewDao interviewDao) {
        this.candidateDao = candidateDao;
        this.jobDao = jobDao;
        this.applicationDao = applicationDao;
        this.screeningResultDao = screeningResultDao;
        this.interviewDao = interviewDao;
    }

    public void exportMasterRecruitmentReport(File outputFile) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // Sheet 1: Candidates
            createCandidatesSheet(workbook, headerStyle, dataStyle, titleStyle);

            // Sheet 2: Jobs
            createJobsSheet(workbook, headerStyle, dataStyle, titleStyle);

            // Sheet 3: Applications Pipeline
            createApplicationsSheet(workbook, headerStyle, dataStyle, titleStyle);

            // Sheet 4: Screening & ATS Results
            createScreeningSheet(workbook, headerStyle, dataStyle, titleStyle);

            // Sheet 5: Interviews & Evaluations
            createInterviewsSheet(workbook, headerStyle, dataStyle, titleStyle);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
            log.info("[HireSmart POI] Exported master Excel report to {}", outputFile.getAbsolutePath());
        }
    }

    public void exportCandidatesReport(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createCandidatesSheet(workbook, createHeaderStyle(workbook), createDataStyle(workbook), createTitleStyle(workbook));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public void exportJobsReport(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createJobsSheet(workbook, createHeaderStyle(workbook), createDataStyle(workbook), createTitleStyle(workbook));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public void exportApplicationsReport(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createApplicationsSheet(workbook, createHeaderStyle(workbook), createDataStyle(workbook), createTitleStyle(workbook));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public void exportScreeningReport(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createScreeningSheet(workbook, createHeaderStyle(workbook), createDataStyle(workbook), createTitleStyle(workbook));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public void exportInterviewsReport(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            createInterviewsSheet(workbook, createHeaderStyle(workbook), createDataStyle(workbook), createTitleStyle(workbook));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private void createCandidatesSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Candidate Directory");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HireSmart ATS — Candidate Profiles & Qualifications");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(r++);
        String[] headers = {"ID", "Full Name", "Email", "Phone", "Headline", "Location", "Qualification", "Experience (Yrs)", "Profile %", "Skills"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<CandidateProfile> list = candidateDao.findAll();
        for (CandidateProfile c : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getFullName() != null ? c.getFullName() : "");
            row.createCell(2).setCellValue(c.getEmail() != null ? c.getEmail() : "");
            row.createCell(3).setCellValue(c.getPhone() != null ? c.getPhone() : "");
            row.createCell(4).setCellValue(c.getHeadline() != null ? c.getHeadline() : "");
            row.createCell(5).setCellValue(c.getLocation() != null ? c.getLocation() : "");
            row.createCell(6).setCellValue(c.getQualification() != null ? c.getQualification() : "");
            row.createCell(7).setCellValue(c.getExperienceYears());
            row.createCell(8).setCellValue(c.getProfileCompletion() + "%");
            row.createCell(9).setCellValue(c.getSkills() != null ? c.getSkills() : "");

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null) row.getCell(i).setCellStyle(dataStyle);
            }
        }
        autoSize(sheet, headers.length);
    }

    private void createJobsSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Job Positions");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HireSmart ATS — Job Postings & Requisitions");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(r++);
        String[] headers = {"Job ID", "Title", "Company", "Department", "Location", "Job Type", "Min Exp (Yrs)", "Required Skills", "Status", "Deadline"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Job> list = jobDao.findAll();
        for (Job j : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(j.getId());
            row.createCell(1).setCellValue(j.getTitle());
            row.createCell(2).setCellValue(j.getCompanyName());
            row.createCell(3).setCellValue(j.getDepartment() != null ? j.getDepartment() : "");
            row.createCell(4).setCellValue(j.getLocation());
            row.createCell(5).setCellValue(j.getJobType());
            row.createCell(6).setCellValue(j.getExperienceRequired());
            row.createCell(7).setCellValue(j.getRequiredSkills());
            row.createCell(8).setCellValue(j.getStatus());
            row.createCell(9).setCellValue(j.getApplicationDeadline() != null ? j.getApplicationDeadline().toString() : "Open");

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null) row.getCell(i).setCellStyle(dataStyle);
            }
        }
        autoSize(sheet, headers.length);
    }

    private void createApplicationsSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Application Pipeline");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HireSmart ATS — Recruitment Funnel & Applicant Status");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(r++);
        String[] headers = {"App ID", "Candidate Name", "Email", "Job Title", "Company", "Match Score", "Status", "Applied Date", "Recruiter Notes"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Application> list = applicationDao.findAll();
        for (Application a : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(a.getId());
            row.createCell(1).setCellValue(a.getCandidateName() != null ? a.getCandidateName() : "");
            row.createCell(2).setCellValue(a.getCandidateEmail() != null ? a.getCandidateEmail() : "");
            row.createCell(3).setCellValue(a.getJobTitle() != null ? a.getJobTitle() : "");
            row.createCell(4).setCellValue(a.getCompanyName() != null ? a.getCompanyName() : "");
            row.createCell(5).setCellValue(a.getMatchScore() + "%");
            row.createCell(6).setCellValue(a.getStatus());
            row.createCell(7).setCellValue(a.getAppliedAt() != null ? a.getAppliedAt().format(DATE_FMT) : "");
            row.createCell(8).setCellValue(a.getRecruiterNotes() != null ? a.getRecruiterNotes() : "");

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null) row.getCell(i).setCellStyle(dataStyle);
            }
        }
        autoSize(sheet, headers.length);
    }

    private void createScreeningSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("ATS Screening Results");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HireSmart ATS — AI & Algorithmic Resume Screening Results");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(r++);
        String[] headers = {"ID", "Candidate", "Target Job", "Overall Match", "ATS Format %", "Skill Match %", "Exp Match %", "Matched Skills", "Missing Skills", "Engine Type"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<ScreeningResult> list = screeningResultDao.findAll();
        for (ScreeningResult s : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(s.getId());
            row.createCell(1).setCellValue(s.getCandidateName() != null ? s.getCandidateName() : "");
            row.createCell(2).setCellValue(s.getJobTitle() != null ? s.getJobTitle() : "");
            row.createCell(3).setCellValue(s.getOverallMatchScore() + "%");
            row.createCell(4).setCellValue(s.getAtsCompatibilityScore() + "%");
            row.createCell(5).setCellValue(s.getSkillMatchScore() + "%");
            row.createCell(6).setCellValue(s.getExperienceMatchScore() + "%");
            row.createCell(7).setCellValue(s.getMatchedSkills() != null ? s.getMatchedSkills() : "");
            row.createCell(8).setCellValue(s.getMissingSkills() != null ? s.getMissingSkills() : "");
            row.createCell(9).setCellValue(s.getEngineType());

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null) row.getCell(i).setCellStyle(dataStyle);
            }
        }
        autoSize(sheet, headers.length);
    }

    private void createInterviewsSheet(XSSFWorkbook workbook, CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        Sheet sheet = workbook.createSheet("Interviews & Evaluations");
        int r = 0;

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HireSmart ATS — Interview Schedules & Candidate Evaluations");
        titleCell.setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(r++);
        String[] headers = {"ID", "Candidate Name", "Job Title", "Round", "Date & Time", "Interviewer", "Status", "Tech Score", "Comm Score", "Problem Solving", "Avg Score", "Recommendation"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Interview> list = interviewDao.findAll();
        for (Interview iv : list) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(iv.getId());
            row.createCell(1).setCellValue(iv.getCandidateName() != null ? iv.getCandidateName() : "");
            row.createCell(2).setCellValue(iv.getJobTitle() != null ? iv.getJobTitle() : "");
            row.createCell(3).setCellValue(iv.getRoundType());
            row.createCell(4).setCellValue(iv.getScheduledTime() != null ? iv.getScheduledTime().format(DATE_FMT) : "");
            row.createCell(5).setCellValue(iv.getInterviewerName());
            row.createCell(6).setCellValue(iv.getStatus());
            row.createCell(7).setCellValue(iv.getTechnicalScore());
            row.createCell(8).setCellValue(iv.getCommunicationScore());
            row.createCell(9).setCellValue(iv.getProblemSolvingScore());
            row.createCell(10).setCellValue(iv.getAverageScore());
            row.createCell(11).setCellValue(iv.getRecommendation());

            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) != null) row.getCell(i).setCellStyle(dataStyle);
            }
        }
        autoSize(sheet, headers.length);
    }

    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(new XSSFColor(new java.awt.Color(37, 99, 235), null));
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(37, 99, 235), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void autoSize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(12000, currentWidth + 1200));
        }
    }
}
