package com.okits02.analys_service.viewmodel.dto.request;

import com.okits02.analys_service.enums.PeriodType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChartQueryRequest {
    PeriodType periodType;
    Integer year;
    LocalDate fromDate;
    LocalDate toDate;
}
