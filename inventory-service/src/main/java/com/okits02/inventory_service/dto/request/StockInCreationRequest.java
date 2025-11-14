package com.okits02.inventory_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInCreationRequest {
    @NotBlank
    String supplierName;
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9\\-]+$")
    @Size(max = 50)
    String referenceCode;
    @Size(max = 255)
    String note;
    @NotEmpty
    @Valid
    List<StockInItemRequest> items;
}
