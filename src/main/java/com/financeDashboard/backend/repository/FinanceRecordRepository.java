package com.financeDashboard.backend.repository;

import com.financeDashboard.backend.entity.FinanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long> {
}
