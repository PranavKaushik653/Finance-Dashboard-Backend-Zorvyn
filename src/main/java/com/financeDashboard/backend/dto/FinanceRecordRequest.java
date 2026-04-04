package com.financeDashboard.backend.dto;

import com.financeDashboard.backend.entity.Category;
import com.financeDashboard.backend.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinanceRecordRequest {
    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
    private BigDecimal amount;
    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    private TransactionType type;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate recordDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
