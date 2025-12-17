package com.example.promotion_service.scheduler;

import com.example.promotion_service.controller.PromotionController;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.repository.PromotionRepository;
import com.example.promotion_service.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionStatusScheduler {
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;

    @Scheduled(fixedRate = 300000)
    public void updateExpiredPromotion() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotionList = promotionRepository.findExpiredPromotion(now);
        for(Promotion p : promotionList){
            p.setActive(false);
            promotionRepository.save(p);
            promotionService.UpdatePromotionStatus(p.getId());
        }
    }
}
