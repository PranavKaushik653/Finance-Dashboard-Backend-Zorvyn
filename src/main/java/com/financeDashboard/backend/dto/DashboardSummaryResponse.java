package com.financeDashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;

    // e.g. { "SALARY": 100000, "RENT": 6000 }
    private Map<String, BigDecimal> categoryWiseTotals;

    private List<FinanceRecordResponse> recentActivity;

    private List<MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private int year;
        private int month;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }
}
