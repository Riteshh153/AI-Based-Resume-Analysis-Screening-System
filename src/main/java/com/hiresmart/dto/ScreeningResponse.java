package com.hiresmart.dto;

import com.hiresmart.model.ScreeningResult;
import java.util.List;

public class ScreeningResponse {
    private boolean success;
    private String message;
    private ScreeningResult result;
    private List<String> matchedSkillsList;
    private List<String> missingSkillsList;
    private List<String> recommendationsList;

    public ScreeningResponse() {}

    public ScreeningResponse(boolean success, String message, ScreeningResult result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ScreeningResult getResult() { return result; }
    public void setResult(ScreeningResult result) { this.result = result; }

    public List<String> getMatchedSkillsList() { return matchedSkillsList; }
    public void setMatchedSkillsList(List<String> matchedSkillsList) { this.matchedSkillsList = matchedSkillsList; }

    public List<String> getMissingSkillsList() { return missingSkillsList; }
    public void setMissingSkillsList(List<String> missingSkillsList) { this.missingSkillsList = missingSkillsList; }

    public List<String> getRecommendationsList() { return recommendationsList; }
    public void setRecommendationsList(List<String> recommendationsList) { this.recommendationsList = recommendationsList; }

    public int getScore() {
        return result != null ? result.getOverallMatchScore() : 0;
    }

    public int getMatchScore() {
        return result != null ? result.getOverallMatchScore() : 0;
    }

    public List<String> getMatchedSkills() {
        return matchedSkillsList;
    }

    public List<String> getMissingSkills() {
        return missingSkillsList;
    }

    public String getRecommendations() {
        return result != null ? result.getRecommendations() : "";
    }
}
