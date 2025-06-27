package com.example.promotion_service.scheduler;

import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionStatusScheduler {
    private final PromotionRepository promotionRepository;

    @Scheduled(fixedRate = 300000)
    public void updateExpiredPromotion() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotionList = promotionRepository.findExpiredPromotion(now);
        for(Promotion p : promotionList){
            p.setActive(false);
            promotionRepository.save(p);
        }
    }
}
