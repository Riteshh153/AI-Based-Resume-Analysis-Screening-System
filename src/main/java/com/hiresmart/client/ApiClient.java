package com.hiresmart.client;

import com.hiresmart.MainApp;
import com.hiresmart.dto.*;
import com.hiresmart.model.*;
import com.hiresmart.service.*;
import javafx.application.Platform;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ApiClient {
    private static ApiClient instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private ApiClient() {}

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private ApplicationContext getContext() {
        return MainApp.getSpringContext();
    }

    // 1. Authentication
    public void loginAsync(AuthRequest request, Consumer<AuthResponse> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            AuthService service = getContext().getBean(AuthService.class);
            return service.login(request);
        }, executor).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                UserSession.getInstance().setSession(res.getUser(), res.getCandidateProfile(), res.getToken());
            }
            onSuccess.accept(res);
        })).exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void registerAsync(RegisterRequest request, Consumer<AuthResponse> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            AuthService service = getContext().getBean(AuthService.class);
            return service.register(request);
        }, executor).thenAccept(res -> Platform.runLater(() -> {
            if (res.isSuccess()) {
                UserSession.getInstance().setSession(res.getUser(), res.getCandidateProfile(), res.getToken());
            }
            onSuccess.accept(res);
        })).exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 2. Jobs
    public void getActiveJobsAsync(Consumer<List<Job>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            JobService service = getContext().getBean(JobService.class);
            return service.getActiveJobs();
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getAllJobsAsync(Consumer<List<Job>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            JobService service = getContext().getBean(JobService.class);
            return service.getAllJobs();
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void createJobAsync(Job job, Consumer<Long> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            JobService service = getContext().getBean(JobService.class);
            return service.createJob(job, UserSession.getInstance().getEmail());
        }, executor).thenAccept(id -> Platform.runLater(() -> onSuccess.accept(id)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 3. Resume Upload & Parsing
    public void uploadResumeAsync(Long candidateId, File file, String originalName, Consumer<Resume> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ResumeService service = getContext().getBean(ResumeService.class);
            try {
                return service.uploadAndParseResume(candidateId, file, originalName);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executor).thenAccept(r -> Platform.runLater(() -> onSuccess.accept(r)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getCandidateResumesAsync(Long candidateId, Consumer<List<Resume>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ResumeService service = getContext().getBean(ResumeService.class);
            return service.getResumesByCandidateId(candidateId);
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 4. ATS Screening
    public void screenResumeAsync(Long resumeId, Long jobId, Consumer<ScreeningResponse> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ScreeningService service = getContext().getBean(ScreeningService.class);
            return service.screenResumeAgainstJob(resumeId, jobId);
        }, executor).thenAccept(res -> Platform.runLater(() -> onSuccess.accept(res)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getAllScreeningResultsAsync(Consumer<List<ScreeningResult>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ScreeningService service = getContext().getBean(ScreeningService.class);
            return service.getAllResults();
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 5. Applications
    public void applyAsync(ApplicationRequest request, Consumer<Application> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ApplicationService service = getContext().getBean(ApplicationService.class);
            return service.apply(request, UserSession.getInstance().getEmail(), UserSession.getInstance().getDisplayName());
        }, executor).thenAccept(app -> Platform.runLater(() -> onSuccess.accept(app)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getApplicationsByCandidateAsync(Long candidateId, Consumer<List<Application>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ApplicationService service = getContext().getBean(ApplicationService.class);
            return service.getApplicationsByCandidate(candidateId);
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getAllApplicationsAsync(Consumer<List<Application>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ApplicationService service = getContext().getBean(ApplicationService.class);
            return service.getAllApplications();
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void updateApplicationStatusAsync(Long appId, String newStatus, String reason, String notes,
                                            Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ApplicationService service = getContext().getBean(ApplicationService.class);
            Long userId = UserSession.getInstance().getUser() != null ? UserSession.getInstance().getUser().getId() : null;
            return service.updateStatus(appId, newStatus, reason, notes, userId, UserSession.getInstance().getEmail());
        }, executor).thenAccept(ok -> Platform.runLater(() -> onSuccess.accept(ok)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 6. Interviews
    public void scheduleInterviewAsync(InterviewScheduleRequest request, Consumer<Interview> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            InterviewService service = getContext().getBean(InterviewService.class);
            return service.scheduleInterview(request, UserSession.getInstance().getEmail());
        }, executor).thenAccept(iv -> Platform.runLater(() -> onSuccess.accept(iv)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getAllInterviewsAsync(Consumer<List<Interview>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            InterviewService service = getContext().getBean(InterviewService.class);
            return service.getAllInterviews();
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void getCandidateInterviewsAsync(Long candidateId, Consumer<List<Interview>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            InterviewService service = getContext().getBean(InterviewService.class);
            return service.getInterviewsByCandidate(candidateId);
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void submitInterviewFeedbackAsync(Long interviewId, InterviewFeedbackRequest request,
                                            Consumer<Boolean> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            InterviewService service = getContext().getBean(InterviewService.class);
            return service.submitFeedback(interviewId, request, UserSession.getInstance().getEmail());
        }, executor).thenAccept(ok -> Platform.runLater(() -> onSuccess.accept(ok)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 7. Mock Interview
    public void generateMockQuestionsAsync(Long candidateId, Long jobId, Consumer<List<MockInterviewQA>> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            MockInterviewService service = getContext().getBean(MockInterviewService.class);
            return service.generateQuestionsForCandidate(candidateId, jobId);
        }, executor).thenAccept(list -> Platform.runLater(() -> onSuccess.accept(list)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    public void submitMockAnswerAsync(Long questionId, String answer, Consumer<MockInterviewQA> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            MockInterviewService service = getContext().getBean(MockInterviewService.class);
            return service.submitAndEvaluateAnswer(questionId, answer);
        }, executor).thenAccept(qa -> Platform.runLater(() -> onSuccess.accept(qa)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 8. Dashboard Statistics
    public void getDashboardStatsAsync(Consumer<DashboardStats> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            DashboardStatsService service = getContext().getBean(DashboardStatsService.class);
            return service.getStats();
        }, executor).thenAccept(stats -> Platform.runLater(() -> onSuccess.accept(stats)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }

    // 9. Excel Reports
    public void exportReportAsync(String reportType, File destination, Consumer<File> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture.supplyAsync(() -> {
            ExcelReportService service = getContext().getBean(ExcelReportService.class);
            try {
                switch (reportType.toLowerCase()) {
                    case "candidates" -> service.exportCandidatesReport(destination);
                    case "jobs" -> service.exportJobsReport(destination);
                    case "applications" -> service.exportApplicationsReport(destination);
                    case "screening" -> service.exportScreeningReport(destination);
                    case "interviews" -> service.exportInterviewsReport(destination);
                    default -> service.exportMasterRecruitmentReport(destination);
                }
                return destination;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor).thenAccept(file -> Platform.runLater(() -> onSuccess.accept(file)))
        .exceptionally(ex -> {
            Platform.runLater(() -> onError.accept(ex));
            return null;
        });
    }
}
