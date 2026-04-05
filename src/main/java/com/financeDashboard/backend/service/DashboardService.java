package com.financeDashboard.backend.service;

import com.financeDashboard.backend.dto.DashboardSummaryResponse;
import com.financeDashboard.backend.dto.FinanceRecordResponse;
import com.financeDashboard.backend.entity.FinanceRecord;
import com.financeDashboard.backend.entity.TransactionType;
import com.financeDashboard.backend.repository.FinanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private FinanceRecordRepository recordRepository;

    @Autowired
    private FinanceRecordService recordService;

    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Category-wise totals
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        List<Object[]> categoryRows = recordRepository.getCategoryTotals();
        for (Object[] row : categoryRows) {
            categoryTotals.put(row[0].toString(), (BigDecimal) row[1]);
        }

        // Recent 10 activity
        List<FinanceRecord> recentRecords =
                recordRepository.findRecentActivity(PageRequest.of(0, 10));
        List<FinanceRecordResponse> recentActivity = recentRecords.stream()
                .map(recordService::toResponse)
                .collect(Collectors.toList());

        // Monthly trends
        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryWiseTotals(categoryTotals)
                .recentActivity(recentActivity)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private List<DashboardSummaryResponse.MonthlyTrend> buildMonthlyTrends() {
        List<Object[]> rows = recordRepository.getMonthlyTrends();

        // Group by year+month
        Map<String, DashboardSummaryResponse.MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = TransactionType.valueOf(row[2].toString());
            BigDecimal amount = (BigDecimal) row[3];

            String key = year + "-" + String.format("%02d", month);
            trendMap.putIfAbsent(key, DashboardSummaryResponse.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            DashboardSummaryResponse.MonthlyTrend trend = trendMap.get(key);
            if (type == TransactionType.INCOME) {
                trend.setIncome(amount);
            } else {
                trend.setExpense(amount);
            }
            trend.setNet(trend.getIncome().subtract(trend.getExpense()));
        }

        return new ArrayList<>(trendMap.values());
    }

    public Map<String, BigDecimal> getIncomeByCategory() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        List<Object[]> rows = recordRepository.getCategoryTotalsByType(TransactionType.INCOME);
        for (Object[] row : rows) {
            result.put(row[0].toString(), (BigDecimal) row[1]);
        }
        return result;
    }

    public Map<String, BigDecimal> getExpenseByCategory() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        List<Object[]> rows = recordRepository.getCategoryTotalsByType(TransactionType.EXPENSE);
        for (Object[] row : rows) {
            result.put(row[0].toString(), (BigDecimal) row[1]);
        }
        return result;
    }
}
