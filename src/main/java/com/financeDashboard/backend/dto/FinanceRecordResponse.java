package com.financeDashboard.backend.dto;

import com.financeDashboard.backend.model.Category;
import com.financeDashboard.backend.model.TransactionType;
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
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}