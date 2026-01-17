package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import com.okits02.analys_service.viewmodel.OrderStatusChartPoint;
import com.okits02.analys_service.viewmodel.dto.request.ChartQueryRequest;
import com.okits02.analys_service.model.OrderAnalysis;
import com.okits02.analys_service.model.StockInAnalysis;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import com.okits02.analys_service.enums.PeriodType;
import com.okits02.analys_service.viewmodel.RevenueStockChartPoint;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final ElasticsearchOperations elasticsearchOperations;
    public List<RevenueStockChartPoint> revenueStockChartPoints(
            ChartQueryRequest request
    ){
        DateRange range = resolveDateRange(request);

        Map<String, BigDecimal> revenueMap = aggregate(
                "order_analysis",
                "orderDate",
                "totalPrice",
                range.interval,
                range.to,
                range.from,
                OrderAnalysis.class
        );

        Map<String, BigDecimal> stockInMap = aggregate(
                "stock_in_analysis",
                "createdAt",
                "totalAmount",
                range.interval,
                range.to,
                range.from,
                StockInAnalysis.class
        );

        Set<String> keys = new TreeSet<>();
        keys.addAll(revenueMap.keySet());
        keys.addAll(stockInMap.keySet());

        List<RevenueStockChartPoint> result = new ArrayList<>();
        for (String key : keys) {
            result.add(
                    new RevenueStockChartPoint(
                            key,
                            revenueMap.getOrDefault(key, BigDecimal.ZERO),
                            stockInMap.getOrDefault(key, BigDecimal.ZERO)
                    )
            );
        }

        return result;
    }

    private Map<String, BigDecimal> aggregate(
            String index,
            String dateField,
            String sumField,
            CalendarInterval interval,
            Instant to,
            Instant from,
            Class<?> entityClass
    ){
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r
                        .date(d -> d
                                .field(dateField)
                                .gte(from.toString()) // ISO-8601
                                .lte(to.toString())
                        )
                ))
                .withAggregation("chart",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field(dateField)
                                        .calendarInterval(interval)
                                        .timeZone("Asia/Ho_Chi_Minh")
                                        .minDocCount(0)
                                        .extendedBounds(ExtendedBounds.of(eb -> eb
                                                .min(from.toString())
                                                .max(to.toString())
                                        ))
                                )
                                .aggregations("total", sa -> sa
                                        .sum(s -> s.field(sumField))
                                )
                        )
                )
                .build();

        SearchHits<?> hits = elasticsearchOperations.search(
                nativeQuery,
                entityClass,
                IndexCoordinates.of(index)
        );
        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) hits.getAggregations();
        ElasticsearchAggregation chartAggWrapper =
                aggregations.get("chart");
        Aggregate chartAgg = chartAggWrapper.aggregation().getAggregate();
        DateHistogramAggregate dateHistogram =
                chartAgg.dateHistogram();
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        for (var bucket : dateHistogram.buckets().array()) {
            double total = bucket
                    .aggregations()
                    .get("total")
                    .sum()
                    .value();
            result.put(
                    bucket.keyAsString(),
                    BigDecimal.valueOf(total)
            );
        }

        return result;
    }

    public List<OrderStatusChartPoint> orderStatusChart(
            ChartQueryRequest request
    ) {
        DateRange range = resolveDateRange(request);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r
                        .date(d -> d
                                .field("orderDate")
                                .gte(range.from.toString())
                                .lte(range.to.toString())
                        )
                ))
                .withAggregation("chart",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field("orderDate")
                                        .calendarInterval(range.interval)
                                )
                                .aggregations("completed", fa -> fa
                                        .filter(f -> f.term(t -> t
                                                .field("orderStatus")
                                                .value("COMPLETED")
                                        ))
                                        .aggregations("count",
                                                sa -> sa.valueCount(v -> v.field("orderId")))
                                )
                                .aggregations("cancelled", fa -> fa
                                        .filter(f -> f.term(t -> t
                                                .field("orderStatus")
                                                .value("CANCELLED")
                                        ))
                                        .aggregations("count",
                                                sa -> sa.valueCount(v -> v.field("orderId")))
                                )
                        )
                )
                .build();

        SearchHits<OrderAnalysis> hits = elasticsearchOperations.search(
                query,
                OrderAnalysis.class,
                IndexCoordinates.of("order_analysis")
        );

        ElasticsearchAggregations aggs =
                (ElasticsearchAggregations) hits.getAggregations();

        DateHistogramAggregate histogram =
                aggs.get("chart")
                        .aggregation()
                        .getAggregate()
                        .dateHistogram();

        List<OrderStatusChartPoint> result = new ArrayList<>();

        for (var bucket : histogram.buckets().array()) {

            long completed = (long) bucket.aggregations()
                    .get("completed")
                    .filter()
                    .aggregations()
                    .get("count")
                    .valueCount()
                    .value();

            long cancelled = (long) bucket.aggregations()
                    .get("cancelled")
                    .filter()
                    .aggregations()
                    .get("count")
                    .valueCount()
                    .value();

            result.add(new OrderStatusChartPoint(
                    bucket.keyAsString(),
                    completed,
                    cancelled
            ));
        }

        return result;
    }


    private boolean existsDataInYear(int year) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("orderDate")
                                        .gte(LocalDate.of(year, 1, 1).toString())
                                        .lte(LocalDate.of(year, 12, 31).toString())
                                )
                        )
                )
                .withMaxResults(1)
                .build();

        SearchHits<?> hits = elasticsearchOperations.search(
                query,
                OrderAnalysis.class,
                IndexCoordinates.of("order_analysis")
        );

        return hits.hasSearchHits();
    }

    private DateRange resolveDateRange(ChartQueryRequest req) {
        DateRange r = new DateRange();
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zone);

        switch (req.getPeriodType()) {

            case RANGE -> {
                if (req.getFromDate() == null || req.getToDate() == null) {
                    throw new IllegalArgumentException("fromDate and toDate must not be null");
                }

                r.from = req.getFromDate()
                        .atStartOfDay(zone)
                        .toInstant();

                r.to = req.getToDate()
                        .atTime(23, 59, 59)
                        .atZone(zone)
                        .toInstant();

                r.interval = CalendarInterval.Day;
            }

            case YEAR -> {
                int year = req.getYear();
                if (year <= 0) {
                    throw new IllegalArgumentException("year must be provided");
                }

                LocalDate start = LocalDate.of(year, 1, 1);
                LocalDate end =
                        (year == today.getYear())
                                ? today
                                : LocalDate.of(year, 12, 31);

                r.from = start.atStartOfDay(zone).toInstant();
                r.to   = end.atTime(23, 59, 59).atZone(zone).toInstant();

                r.interval = CalendarInterval.Month;
            }
        }

        return r;
    }


    private static class DateRange {
        Instant from;
        Instant to;
        CalendarInterval interval;
    }
}


