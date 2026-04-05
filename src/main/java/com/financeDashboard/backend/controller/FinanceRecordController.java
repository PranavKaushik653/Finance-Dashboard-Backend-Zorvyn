package com.financeDashboard.backend.controller;

import com.financeDashboard.backend.dto.ApiResponse;
import com.financeDashboard.backend.dto.FinanceRecordRequest;
import com.financeDashboard.backend.dto.FinanceRecordResponse;
import com.financeDashboard.backend.dto.PagedResponse;
import com.financeDashboard.backend.entity.Category;
import com.financeDashboard.backend.entity.TransactionType;
import com.financeDashboard.backend.service.FinanceRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
public class FinanceRecordController {

    @Autowired
    private FinanceRecordService recordService;

    /**
     * GET /api/records
     * ANALYST and ADMIN can view records.
     * Supports filtering by type, category, date range + pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<FinanceRecordResponse>>> getAllRecords(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<FinanceRecordResponse> result =
                recordService.getAllRecords(type, category, startDate, endDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/records/{id}
     * ANALYST and ADMIN can view a specific record.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<ApiResponse<FinanceRecordResponse>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getRecordById(id)));
    }

    /**
     * POST /api/records
     * ADMIN only: create a financial record.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinanceRecordResponse>> createRecord(
            @Valid @RequestBody FinanceRecordRequest request,
            Authentication authentication) {
        FinanceRecordResponse created =
                recordService.createRecord(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Financial record created successfully"));
    }

    /**
     * PUT /api/records/{id}
     * ADMIN only: update a record.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinanceRecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinanceRecordRequest request) {
        FinanceRecordResponse updated = recordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Financial record updated successfully"));
    }

    /**
     * DELETE /api/records/{id}
     * ADMIN only: soft-delete a record.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Financial record deleted successfully"));
    }
}

