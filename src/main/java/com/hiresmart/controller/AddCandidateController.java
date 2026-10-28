package com.hiresmart.controller;

import com.hiresmart.client.ApiClient;
import com.hiresmart.client.UserSession;
import com.hiresmart.model.MockInterviewQA;
import com.hiresmart.service.MockInterviewService;
import com.hiresmart.util.AnimationUtils;
import com.hiresmart.util.UiUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddCandidateController {

    @FXML private Label categoryTag;
    @FXML private Label questionPromptLabel;
    @FXML private TextArea candidateAnswerArea;
    @FXML private Button btnSubmitAnswer;

    @FXML private VBox evaluationResultCard;
    @FXML private Label mockScoreLabel;
    @FXML private Label mockFeedbackLabel;

    @FXML private TableView<MockInterviewQA> mockHistoryTable;
    @FXML private TableColumn<MockInterviewQA, Long> colQId;
    @FXML private TableColumn<MockInterviewQA, String> colCategory;
    @FXML private TableColumn<MockInterviewQA, String> colQuestion;
    @FXML private TableColumn<MockInterviewQA, Integer> colScore;
    @FXML private TableColumn<MockInterviewQA, String> colFeedback;

    private final MockInterviewService mockInterviewService;
    private MockInterviewQA currentQuestion;

    public AddCandidateController(MockInterviewService mockInterviewService) {
        this.mockInterviewService = mockInterviewService;
    }

    @FXML
    public void initialize() {
        colQId.setCellValueFactory(q -> new SimpleObjectProperty<>(q.getValue().getId()));
        colCategory.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getCategory()));
        colQuestion.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getQuestionText()));
        colScore.setCellValueFactory(q -> new SimpleIntegerProperty(q.getValue().getScore()).asObject());
        colFeedback.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().getAiFeedback()));

        colScore.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null || score == 0) {
                    setText("-");
                } else {
                    setText(score + "/100");
                    if (score >= 85) setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    else if (score >= 70) setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                }
            }
        });

        loadPracticeSession();
    }

    private void loadPracticeSession() {
        Long candidateId = getCandidateId();
        List<MockInterviewQA> list = mockInterviewService.getCandidateMockInterviews(candidateId);
        if (list.isEmpty()) {
            list = mockInterviewService.generateQuestionsForCandidate(candidateId, 1L);
        }

        mockHistoryTable.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) {
            displayQuestion(list.get(0));
        }
    }

    private void displayQuestion(MockInterviewQA qa) {
        this.currentQuestion = qa;
        categoryTag.setText(qa.getCategory());
        questionPromptLabel.setText(qa.getQuestionText());
        candidateAnswerArea.setText(qa.getCandidateAnswer() != null ? qa.getCandidateAnswer() : "");

        if (qa.getScore() > 0) {
            mockScoreLabel.setText(String.valueOf(qa.getScore()));
            mockFeedbackLabel.setText(qa.getAiFeedback());
            evaluationResultCard.setVisible(true);
            evaluationResultCard.setManaged(true);
        } else {
            evaluationResultCard.setVisible(false);
            evaluationResultCard.setManaged(false);
        }
    }

    @FXML
    public void onGenerateNewQuestions() {
        Long candidateId = getCandidateId();
        List<MockInterviewQA> questions = mockInterviewService.generateQuestionsForCandidate(candidateId, 1L);
        mockHistoryTable.setItems(FXCollections.observableArrayList(questions));
        if (!questions.isEmpty()) {
            displayQuestion(questions.get(0));
            UiUtils.showInfo("Questions Generated", "Generated 4 new technical & behavioral practice questions for your profile!");
        }
    }

    @FXML
    public void onSubmitAnswer() {
        if (currentQuestion == null) return;
        String answer = candidateAnswerArea.getText();
        if (answer == null || answer.trim().length() < 15) {
            UiUtils.showError("Answer Too Short", "Please provide a more detailed technical explanation (at least a couple sentences).");
            return;
        }

        btnSubmitAnswer.setText("Evaluating...");
        btnSubmitAnswer.setDisable(true);

        ApiClient.getInstance().submitMockAnswerAsync(currentQuestion.getId(), answer, evaluated -> {
            btnSubmitAnswer.setText("⚡ Submit for AI Evaluation");
            btnSubmitAnswer.setDisable(false);

            mockScoreLabel.setText(String.valueOf(evaluated.getScore()));
            mockFeedbackLabel.setText(evaluated.getAiFeedback());

            evaluationResultCard.setVisible(true);
            evaluationResultCard.setManaged(true);
            AnimationUtils.scaleIn(evaluationResultCard, 350);

            // Refresh table
            loadPracticeSession();
        }, err -> {
            btnSubmitAnswer.setText("⚡ Submit for AI Evaluation");
            btnSubmitAnswer.setDisable(false);
            UiUtils.showError("Evaluation Error", err.getMessage());
        });
    }

    private Long getCandidateId() {
        UserSession session = UserSession.getInstance();
        return (session.getProfile() != null) ? session.getProfile().getId() : 1L;
    }
}
