package com.financeDashboard.backend.service;


import com.financeDashboard.backend.dto.FinanceRecordRequest;
import com.financeDashboard.backend.dto.FinanceRecordResponse;
import com.financeDashboard.backend.dto.PagedResponse;
import com.financeDashboard.backend.entity.Category;
import com.financeDashboard.backend.entity.FinanceRecord;
import com.financeDashboard.backend.entity.TransactionType;
import com.financeDashboard.backend.entity.User;
import com.financeDashboard.backend.exception.ResourceNotFoundException;
import com.financeDashboard.backend.repository.FinanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinanceRecordService {

    @Autowired
    private FinanceRecordRepository recordRepository;

    @Autowired
    private UserService userService;

    public PagedResponse<FinanceRecordResponse> getAllRecords(
            TransactionType type,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FinanceRecord> records = recordRepository.findAllWithFilters(
                type, category, startDate, endDate, pageable);

        Page<FinanceRecordResponse> responsePage = records.map(this::toResponse);
        return PagedResponse.of(responsePage);
    }

    public FinanceRecordResponse getRecordById(Long id) {
        FinanceRecord record = findActiveRecord(id);
        return toResponse(record);
    }

    @Transactional
    public FinanceRecordResponse createRecord(FinanceRecordRequest request, String username) {
        User creator = userService.findUserByUsername(username);

        FinanceRecord record = FinanceRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .recordDate(request.getRecordDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .deleted(false)
                .build();

        return toResponse(recordRepository.save(record));
    }

    @Transactional
    public FinanceRecordResponse updateRecord(Long id, FinanceRecordRequest request) {
        FinanceRecord record = findActiveRecord(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setRecordDate(request.getRecordDate());
        record.setNotes(request.getNotes());

        return toResponse(recordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinanceRecord record = findActiveRecord(id);
        // Soft delete
        record.setDeleted(true);
        recordRepository.save(record);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    public FinanceRecord findActiveRecord(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
    }

    public FinanceRecordResponse toResponse(FinanceRecord record) {
        return FinanceRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .recordDate(record.getRecordDate())
                .notes(record.getNotes())
                .createdBy(record.getCreatedBy().getUsername())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

}
