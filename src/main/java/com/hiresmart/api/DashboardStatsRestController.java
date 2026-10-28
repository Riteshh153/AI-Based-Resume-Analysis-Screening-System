package com.hiresmart.api;

import com.hiresmart.model.DashboardStats;
import com.hiresmart.service.DashboardStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class DashboardStatsRestController {
    private final DashboardStatsService statsService;

    public DashboardStatsRestController(DashboardStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
