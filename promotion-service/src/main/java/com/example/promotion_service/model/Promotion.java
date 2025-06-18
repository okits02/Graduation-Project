package com.example.promotion_service.model;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.UsageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    @Enumerated(EnumType.STRING)
    DiscountType discountType;
    @Enumerated(EnumType.STRING)
    ApplyTo applyTo;
    @Enumerated(EnumType.STRING)
    UsageType usageType;
    String voucherCode;
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    Date startDate;
    Date endDate;
    Double minimumOrderPurchaseAmount;
    int usageLimited;
    int usageCount;
    boolean isActive;
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    PromotionApplyTo promotionApplyTo;
    Date createAt;
    Date deleteAt;

    @Override
    public boolean equals(Object object)
    {
        if(this == object)
        {
            return true;
        }
        if(!(object instanceof Promotion))
        {
            return false;
        }
        return id != null && id.equals(((Promotion) object).id);
    }
}
