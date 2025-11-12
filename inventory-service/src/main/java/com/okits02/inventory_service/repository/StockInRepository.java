package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.StockIn;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StockInRepository extends JpaRepository<StockIn, String> {
    Optional<StockIn> findByReferenceCode(String referenceCode);
    @Query(
            value = """
        SELECT DISTINCT s
        FROM StockIn s
        LEFT JOIN s.items i
        WHERE s.createdAt BETWEEN :start AND :end
        ORDER BY s.createdAt DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT s)
        FROM StockIn s
        LEFT JOIN s.items i
        WHERE s.createdAt BETWEEN :start AND :end
    """
    )
    Page<StockIn> getAllHistory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    boolean existsByReferenceCode(String referenceCode);
}
