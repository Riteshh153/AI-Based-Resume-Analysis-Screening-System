package com.hiresmart.service;

import com.hiresmart.dao.ApplicationDao;
import com.hiresmart.dao.CandidateProfileDao;
import com.hiresmart.dao.ScreeningResultDao;
import com.hiresmart.dao.UserDao;
import com.hiresmart.model.Candidate;
import com.hiresmart.model.CandidateProfile;
import com.hiresmart.model.Role;
import com.hiresmart.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {
    private final CandidateProfileDao profileDao;
    private final UserDao userDao;
    private final ScreeningResultDao screeningResultDao;
    private final ApplicationDao applicationDao;

    public CandidateService(CandidateProfileDao profileDao, UserDao userDao,
                            ScreeningResultDao screeningResultDao, ApplicationDao applicationDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
        this.screeningResultDao = screeningResultDao;
        this.applicationDao = applicationDao;
    }

    public Optional<CandidateProfile> getProfileByUserId(Long userId) {
        return profileDao.findByUserId(userId);
    }

    public Optional<CandidateProfile> getProfileById(Long id) {
        return profileDao.findById(id);
    }

    public boolean updateProfile(CandidateProfile profile, String fullName, String phone) {
        boolean ok = profileDao.update(profile);
        if (profile.getUserId() != null && (fullName != null || phone != null)) {
            userDao.findById(profile.getUserId()).ifPresent(user -> {
                if (fullName != null) user.setFullName(fullName);
                if (phone != null) user.setPhone(phone);
                userDao.update(user);
            });
        }
        return ok;
    }

    public List<CandidateProfile> getAllProfiles() {
        return profileDao.findAll();
    }

    /**
     * Backward-compatible helper that maps database profiles to Candidate view-models for JavaFX tables
     */
    public List<Candidate> findRecent(int limit) {
        List<CandidateProfile> profiles = profileDao.findAll();
        List<Candidate> list = new ArrayList<>();
        int count = 0;
        for (CandidateProfile p : profiles) {
            if (count++ >= limit) break;
            Candidate c = new Candidate();
            c.setId(p.getId());
            c.setFullName(p.getFullName() != null ? p.getFullName() : "Candidate #" + p.getId());
            c.setEmail(p.getEmail() != null ? p.getEmail() : "N/A");
            c.setPhone(p.getPhone());
            c.setAppliedPosition(p.getHeadline() != null ? p.getHeadline() : "Software Engineer");
            c.setYearsExperience(p.getExperienceYears());

            // Get latest screening score if available
            var results = screeningResultDao.findByCandidateId(p.getId());
            int score = results.isEmpty() ? 75 : results.get(0).getOverallMatchScore();
            c.setAiScore(score);

            // Get application status if available
            var apps = applicationDao.findByCandidateId(p.getId());
            c.setStatus(!apps.isEmpty() ? apps.get(0).getStatus() : "Under Review");
            c.setAppliedDate(!apps.isEmpty() && apps.get(0).getAppliedAt() != null ?
                    apps.get(0).getAppliedAt().toLocalDate().toString() : "Recent");

            list.add(c);
        }
        return list;
    }

    public int countAll() {
        return userDao.countByRole(Role.CANDIDATE);
    }

    public int countScreened() {
        return screeningResultDao.countAll();
    }

    public int countInterviews() {
        return applicationDao.countByStatus("INTERVIEW_SCHEDULED");
    }

    public int countSelected() {
        return applicationDao.countByStatus("SELECTED");
    }
}