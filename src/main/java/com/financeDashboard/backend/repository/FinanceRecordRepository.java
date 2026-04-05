package com.financeDashboard.backend.repository;

import com.financeDashboard.backend.entity.Category;
import com.financeDashboard.backend.entity.FinanceRecord;
import com.financeDashboard.backend.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long> {

    // Find non-deleted records with filters and pagination
    @Query("SELECT r FROM FinanceRecord r WHERE r.deleted = false " +
            "AND (:type IS NULL OR r.type = :type) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:startDate IS NULL OR r.recordDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.recordDate <= :endDate)")
    Page<FinanceRecord> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") Category category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    //soft delete id
    Optional<FinanceRecord> findByIdAndDeletedFalse(Long id);

    // Sum by type (for dashboard)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinanceRecord r WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Category-wise totals
    @Query("SELECT r.category, SUM(r.amount) FROM FinanceRecord r " +
            "WHERE r.deleted = false GROUP BY r.category")
    List<Object[]> getCategoryTotals();

    // Monthly trends - income and expense per month
    @Query("SELECT FUNCTION('YEAR', r.recordDate), FUNCTION('MONTH', r.recordDate), r.type, SUM(r.amount) " +
            "FROM FinanceRecord r WHERE r.deleted = false " +
            "GROUP BY FUNCTION('YEAR', r.recordDate), FUNCTION('MONTH', r.recordDate), r.type " +
            "ORDER BY FUNCTION('YEAR', r.recordDate), FUNCTION('MONTH', r.recordDate)")
    List<Object[]> getMonthlyTrends();

    // Recent activity (non-deleted, sorted by date desc)
    @Query("SELECT r FROM FinanceRecord r WHERE r.deleted = false ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<FinanceRecord> findRecentActivity(Pageable pageable);

    // Category-wise totals filtered by type
    @Query("SELECT r.category, SUM(r.amount) FROM FinanceRecord r " +
            "WHERE r.type = :type AND r.deleted = false GROUP BY r.category")
    List<Object[]> getCategoryTotalsByType(@Param("type") TransactionType type);

}
