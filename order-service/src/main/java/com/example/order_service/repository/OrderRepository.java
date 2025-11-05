package com.example.order_service.repository;

import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.OrderSummaryResponse;
import com.example.order_service.enums.Status;
import com.example.order_service.model.Orders;
import com.okits02.common_lib.dto.PageResponse;
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, String> {
    @Query(value = """
            SELECT DISTINCT o
            FROM Orders o
            JOIN FETCH o.items i
            WHERE o.userId =: userId
            ORDER BY o.orderDate DESC
            """, countQuery = """
            SELECT COUNT(DISTINCT o)
            FROM Orders o
            WHERE o.userId =:userId
            """)
    public Page<Orders> findAllByUserId(@Param("userId") String userId,
                                        Pageable pageable);
    @Query(value = """
            SELECT DISTINCT o
            FROM Orders o
            JOIN FETCH o.items i
            WHERE o.userId =: userId
            AND(:status IS NULL OR o.orderStatus = :status)
            ORDER BY o.orderDate DESC
            """, countQuery = """
            SELECT COUNT(DISTINCT o)
            FROM Orders o
            WHERE o.userId =: userId
            AND (:status IS NULL OR o.orderStatus = :status)
            """)
    public Page<Orders> findAllByUserIdAndStatus(@Param("userId") String userId,
                                                 @Param("status") Status status,
                                                 Pageable pageable);
    @Query(
            value = """
        SELECT DISTINCT o
        FROM Orders o
        WHERE o.orderStatus = :status
        ORDER BY o.orderDate DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT o)
        FROM Orders o
        WHERE o.orderStatus = :status
        """
    )
    Page<Orders> findAllByStatus(
            @Param("status") Status status,
            Pageable pageable
    );


}
