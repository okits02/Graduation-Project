package com.example.order_service.model;

import com.example.order_service.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import com.example.order_service.constant.AppConstant;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Orders extends AbstractMappedEntity{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", unique = true, nullable = false, updatable = false)
    private String orderId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = AppConstant.LOCAL_DATE_TIME_FORMAT, shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = AppConstant.LOCAL_DATE_TIME_FORMAT)
    @Column(name = "order_date")
    private  LocalDateTime orderDate;
    @Column(name = "order_desc")
    private String orderDesc;
    @Column(name = "order_fee", columnDefinition = "decimal")
    private BigDecimal orderFee;
    @Column(name = "order_status")
    private Status orderStatus;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "payment_id")
    private String paymentId;
    @Column(name = "delivery_id")
    private String deliveryId;
    @Column(name = "address_id")
    private String addressId;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;


    public void calculateTotalPrice() {
        this.totalPrice = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getSellPrice() != null) {
                this.totalPrice = this.totalPrice.add(item.getSellPrice());
            }
        }
    }
}
