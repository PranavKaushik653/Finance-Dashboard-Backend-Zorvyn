package com.financeDashboard.backend.dto;

import com.financeDashboard.backend.entity.Category;
import com.financeDashboard.backend.entity.TransactionType;
import com.financeDashboard.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRecordResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private Category category;
    private LocalDate recordDate;
    private String notes;
    private User createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}