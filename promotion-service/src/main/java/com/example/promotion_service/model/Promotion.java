package com.example.promotion_service.model;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.enums.UsageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public  class Promotion {
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
    @Enumerated(EnumType.STRING)
    PromotionKind promotionKind;
    String voucherCode;
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    Date startDate;
    Date endDate;
    Double minimumOrderPurchaseAmount;
    Double maxDiscountAmount;
    int usageLimited;
    int usageCount;
    int usageLimitPerUser;
    Boolean active;
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
    LocalDate createAt;
    LocalDate updateAt;

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
