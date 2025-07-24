package com.example.search_service.viewmodel.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusPromotionDTO {
    String id;
}
