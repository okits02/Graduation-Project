package com.okits02.payment_service.repository;

import com.okits02.payment_service.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query(value = """
            SELECT DISTINCT o
            FROM Payment o
            WHERE o.userId =: userId
            ORDER BY o.createdAt DESC
            """, countQuery = """   
            SELECT COUNT(DISTINCT o)
            FROM Payment o
            WHERE o.userId =: userId
            """)
    public Page<Payment> findAllByUserId(@Param("userId") String userId,
                                                 Pageable pageable);
}
