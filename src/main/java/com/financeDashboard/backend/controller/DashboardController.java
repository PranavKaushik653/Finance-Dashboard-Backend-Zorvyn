package com.financeDashboard.backend.controller;

import com.financeDashboard.backend.dto.ApiResponse;
import com.financeDashboard.backend.dto.DashboardSummaryResponse;
import com.financeDashboard.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * All authenticated users (VIEWER, ANALYST, ADMIN) can see the dashboard summary.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    /**
     * GET /api/dashboard/income-by-category
     * ANALYST and ADMIN only.
     */
    @GetMapping("/income-by-category")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getIncomeByCategory() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getIncomeByCategory()));
    }

    /**
     * GET /api/dashboard/expense-by-category
     * ANALYST and ADMIN only.
     */
    @GetMapping("/expense-by-category")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getExpenseByCategory() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getExpenseByCategory()));
    }
}
