package com.example.promotion_service.services.Impl;

import com.example.promotion_service.dto.request.PromotionCampaignRequest;
import com.example.promotion_service.dto.response.PromotionCampaignResponse;
import com.example.promotion_service.exception.PromotionErrorCode;
import com.example.promotion_service.mapper.PromotionCampaignMapper;
import com.example.promotion_service.model.PromotionCampaign;
import com.example.promotion_service.repository.PromotionCampaignRepository;
import com.example.promotion_service.services.PromotionCampaignService;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionCampaignServiceImpl implements PromotionCampaignService {
    private final PromotionCampaignRepository promotionCampaignRepository;
    private final PromotionCampaignMapper promotionCampaignMapper;
    @Override
    public PromotionCampaignResponse save(PromotionCampaignRequest request) {
        if(promotionCampaignRepository.existsByName(request.getName()))
        {
            throw new AppException(PromotionErrorCode.CAMPAIGN_EXISTS);
        }
        PromotionCampaign promotionCampaign = promotionCampaignMapper.toPromotionCampaign(request);
        return promotionCampaignMapper.toResponse(promotionCampaignRepository.save(promotionCampaign));
    }

    @Override
    public PromotionCampaignResponse update(PromotionCampaignRequest request) {
        PromotionCampaign promotionCampaign = promotionCampaignRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(PromotionErrorCode.CAMPAIGN_NOT_EXISTS));
        promotionCampaignMapper.update(promotionCampaign, request);
        return promotionCampaignMapper.toResponse(promotionCampaignRepository.save(promotionCampaign));
    }

    @Override
    public void delete(String id) {
        promotionCampaignRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        promotionCampaignRepository.deleteAll();
    }

    @Override
    public List<PromotionCampaignResponse> getAll() {
        List<PromotionCampaign> promotionCampaigns = promotionCampaignRepository.findAll();
        return promotionCampaigns.stream().map(promotionCampaignMapper::toResponse).toList();
    }
}
