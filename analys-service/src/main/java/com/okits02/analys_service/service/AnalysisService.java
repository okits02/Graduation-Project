package com.okits02.analys_service.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.okits02.analys_service.enums.PeriodType;
import com.okits02.analys_service.enums.Status;
import com.okits02.analys_service.model.InventoryTransactionAnalysis;
import com.okits02.analys_service.model.OrderAnalysis;
import com.okits02.analys_service.model.OrderItem;
import com.okits02.analys_service.model.StockInAnalysis;
import com.okits02.analys_service.viewmodel.OrderStatusChartPoint;
import com.okits02.analys_service.viewmodel.RevenueStockChartPoint;
import com.okits02.analys_service.viewmodel.StockProductTable;
import com.okits02.analys_service.viewmodel.dto.Response.ProductInfoResponse;
import com.okits02.analys_service.viewmodel.dto.request.ChartQueryRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final ElasticsearchOperations elasticsearchOperations;

    public List<RevenueStockChartPoint> statisticDashboard(
            ChartQueryRequest request) {
        PeriodType type = request.getPeriodType();

        List<RevenueStockChartPoint> result = new ArrayList<>();

        if (type == PeriodType.RANGE) {

            if (request.getFromDate() == null || request.getToDate() == null) {
                throw new IllegalArgumentException(
                        "fromDate and toDate are required for RANGE"
                );
            }

            String start = request.getFromDate().toString();
            String end = request.getToDate().toString();

            Map<String, BigDecimal> orderMap =
                    getAggregationForOrder(start, end);

            Map<String, BigDecimal> stockMap =
                    getAggregationForStockIn(start, end);

            for (LocalDate date = request.getFromDate();
                 !date.isAfter(request.getToDate());
                 date = date.plusDays(1)) {

                String key = date.toString();

                result.add(
                        new RevenueStockChartPoint(
                                date,
                                orderMap.getOrDefault(key, BigDecimal.ZERO),
                                stockMap.getOrDefault(key, BigDecimal.ZERO)
                        )
                );
            }

        } else if (type == PeriodType.YEAR) {

            if (request.getYear() == null) {
                throw new IllegalArgumentException(
                        "year is required for YEAR periodType"
                );
            }

            int year = request.getYear();

            Map<String, BigDecimal> orderMap =
                    getMonthlyAggregationForOrder(year);

            Map<String, BigDecimal> stockMap =
                    getMonthlyAggregationForStockIn(year);

            for (int month = 1; month <= 12; month++) {

                YearMonth ym = YearMonth.of(year, month);
                String key = ym.toString();

                result.add(
                        new RevenueStockChartPoint(
                                ym.atDay(1),
                                orderMap.getOrDefault(key, BigDecimal.ZERO),
                                stockMap.getOrDefault(key, BigDecimal.ZERO)
                        )
                );
            }
        }

        return result;
    }

    public List<OrderStatusChartPoint> getOrderStatusCharPoint( ChartQueryRequest request){
        PeriodType type = request.getPeriodType();
        List<OrderStatusChartPoint> result = new ArrayList<>();

        if(type == PeriodType.YEAR){
            Map<String, Map<Status, Long>> agg =
                    getMonthlyOrderCountByStatus(request.getYear());
            return toOrderStatusChartPoints(agg);
        }else if(type == PeriodType.RANGE){
            Map<String, Map<Status, Long>> agg =
                    getDailyOrderCountByStatus(request.getFromDate().toString(), request.getToDate().toString());

            return toOrderStatusChartPoints(agg);
        }
        return result;
    }

    public Map<String, BigDecimal> getAggregationForOrder(
            String start,
            String end
    ){
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("orderDate")
                                        .gte(JsonData.of(start + "T00:00:00").toString())
                                        .lte(JsonData.of(end + "T23:59:59").toString()
                                        )
                                )
                        )
                )
                .withAggregation("order_by_day", Aggregation.of(a
                        -> a
                        .dateHistogram(dh -> dh
                                    .field("orderDate")
                                    .calendarInterval(CalendarInterval.Day)
                                    .format("yyyy-MM-dd")
                                    .minDocCount(0)
                                    .extendedBounds(eb -> eb
                                        .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                        .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                    )
                        ).aggregations("total_price",
                                        Aggregation.of(sa -> sa
                                                .sum(s -> s.field("totalPrice"))
                                        )
                        )
                )
                )
                .build();

        SearchHits<OrderAnalysis> searchHits =
                elasticsearchOperations.search(query, OrderAnalysis.class);

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        AggregationsContainer<?> container = searchHits.getAggregations();
        if (container == null) return result;

        List<ElasticsearchAggregation> aggs =
                (List<ElasticsearchAggregation>) container.aggregations();

        ElasticsearchAggregation orderAggWrapper = aggs.stream()
                .filter(a -> "order_by_day".equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (orderAggWrapper == null) return result;

        Aggregate aggregate = orderAggWrapper.aggregation().getAggregate();
        DateHistogramAggregate histogram = aggregate.dateHistogram();

        for (DateHistogramBucket bucket : histogram.buckets().array()) {

            String date = bucket.keyAsString();

            SumAggregate sum =
                    bucket.aggregations().get("total_price").sum();

            result.put(date, BigDecimal.valueOf(sum.value()));
        }

        return result;
    }

    public Map<String, BigDecimal> getAggregationForStockIn(
            String start,
            String end
    ){
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("createdAt")
                                        .gte(JsonData.of(start + "T00:00:00").toString())
                                        .lte(JsonData.of(end + "T23:59:59").toString()
                                        )
                                )
                        )
                )
                .withAggregation("stock_by_day", Aggregation.of(a
                                -> a
                                .dateHistogram(dh -> dh
                                        .field("createdAt")
                                        .calendarInterval(CalendarInterval.Day)
                                        .format("yyyy-MM-dd")
                                        .minDocCount(0)
                                        .extendedBounds(eb -> eb
                                                .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                                .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                        )
                                ).aggregations("total_amount",
                                        Aggregation.of(sa -> sa
                                                .sum(s -> s.field("totalAmount"))
                                        )
                                )
                        )
                )
                .build();

        SearchHits<OrderAnalysis> searchHits =
                elasticsearchOperations.search(query, OrderAnalysis.class);

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        AggregationsContainer<?> container = searchHits.getAggregations();
        if (container == null) return result;

        List<ElasticsearchAggregation> aggs =
                (List<ElasticsearchAggregation>) container.aggregations();

        ElasticsearchAggregation stockAggWrapper = aggs.stream()
                .filter(a -> "stock_by_day".equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (stockAggWrapper == null) return result;

        DateHistogramAggregate histogram =
                stockAggWrapper.aggregation().getAggregate().dateHistogram();

        for (DateHistogramBucket bucket : histogram.buckets().array()) {

            String date = bucket.keyAsString();

            SumAggregate sum =
                    bucket.aggregations().get("total_amount").sum();

            result.put(date, BigDecimal.valueOf(sum.value()));
        }

        return result;
    }

    public Map<String, BigDecimal> getMonthlyAggregationForOrder(
            int year
    ) {

        String start = year + "-01";
        String end = year + "-12";

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("orderDate")
                                        .gte(JsonData.of(year + "-01-01T00:00:00").toString())
                                        .lte(JsonData.of(year + "-12-31T23:59:59").toString())
                                )
                        )
                )
                .withAggregation("order_by_month",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field("orderDate")
                                        .calendarInterval(CalendarInterval.Month)
                                        .format("yyyy-MM")
                                        .minDocCount(0)
                                        .extendedBounds(eb -> eb
                                                .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                                .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                        )
                                )
                                .aggregations("total_price",
                                        Aggregation.of(sa -> sa
                                                .sum(s -> s.field("totalPrice"))
                                        )
                                )
                        )
                )
                .build();

        SearchHits<OrderAnalysis> searchHits =
                elasticsearchOperations.search(query, OrderAnalysis.class);

        return extractMonthSumAggregation(
                searchHits, "order_by_month", "total_price"
        );
    }

    public Map<String, BigDecimal> getMonthlyAggregationForStockIn(
            int year
    ) {

        String start = year + "-01";
        String end = year + "-12";

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("createdAt")
                                        .gte(JsonData.of(year + "-01-01T00:00:00").toString())
                                        .lte(JsonData.of(year + "-12-31T23:59:59").toString())
                                )
                        )
                )
                .withAggregation("stock_by_month",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field("createdAt")
                                        .calendarInterval(CalendarInterval.Month)
                                        .format("yyyy-MM")
                                        .minDocCount(0)
                                        .extendedBounds(eb -> eb
                                                .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                                .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                        )
                                )
                                .aggregations("total_amount",
                                        Aggregation.of(sa -> sa
                                                .sum(s -> s.field("totalAmount"))
                                        )
                                )
                        )
                )
                .build();

        SearchHits<StockInAnalysis> searchHits =
                elasticsearchOperations.search(query, StockInAnalysis.class);

        return extractMonthSumAggregation(
                searchHits, "stock_by_month", "total_amount"
        );
    }

    private Map<String, BigDecimal> extractMonthSumAggregation(
            SearchHits<?> hits,
            String histogramName,
            String sumAggName
    ) {

        Map<String, BigDecimal> result = new LinkedHashMap<>();

        AggregationsContainer<?> container = hits.getAggregations();
        if (container == null) return result;

        List<ElasticsearchAggregation> aggs =
                (List<ElasticsearchAggregation>) container.aggregations();

        ElasticsearchAggregation wrapper = aggs.stream()
                .filter(a -> histogramName.equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (wrapper == null) return result;

        DateHistogramAggregate histogram =
                wrapper.aggregation().getAggregate().dateHistogram();

        for (DateHistogramBucket bucket : histogram.buckets().array()) {

            SumAggregate sum =
                    bucket.aggregations().get(sumAggName).sum();

            result.put(
                    bucket.keyAsString(),
                    BigDecimal.valueOf(sum.value())
            );
        }

        return result;
    }

    public Map<String, Map<Status, Long>> getDailyOrderCountByStatus(
            String start,
            String end
    ) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("orderDate")
                                        .gte(JsonData.of(start + "T00:00:00").toString())
                                        .lte(JsonData.of(end + "T23:59:59").toString())
                                )
                        )
                )
                .withAggregation("order_by_day",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field("orderDate")
                                        .calendarInterval(CalendarInterval.Day)
                                        .format("yyyy-MM-dd")
                                        .minDocCount(0)
                                        .extendedBounds(eb -> eb
                                                .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                                .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                        )
                                )
                                .aggregations("by_status",
                                        Aggregation.of(ta -> ta
                                                .terms(t -> t
                                                        .field("orderStatus")
                                                )
                                        )
                                )
                        )
                )
                .build();

        SearchHits<OrderAnalysis> hits =
                elasticsearchOperations.search(query, OrderAnalysis.class);

        return extractOrderCountByStatus(hits, "order_by_day");
    }


    public Map<String, Map<Status, Long>> getMonthlyOrderCountByStatus(
            int year
    ) {

        String start = year + "-01";
        String end = year + "-12";

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("orderDate")
                                        .gte(JsonData.of(year + "-01-01T00:00:00").toString())
                                        .lte(JsonData.of(year + "-12-31T23:59:59").toString())
                                )
                        )
                )
                .withAggregation("order_by_month",
                        Aggregation.of(a -> a
                                .dateHistogram(dh -> dh
                                        .field("orderDate")
                                        .calendarInterval(CalendarInterval.Month)
                                        .format("yyyy-MM")
                                        .minDocCount(0)
                                        .extendedBounds(eb -> eb
                                                .min(FieldDateMath.of(fdm -> fdm.expr(start)))
                                                .max(FieldDateMath.of(fdm -> fdm.expr(end)))
                                        )
                                )
                                .aggregations("by_status",
                                        Aggregation.of(ta -> ta
                                                .terms(t -> t
                                                        .field("orderStatus")
                                                )
                                        )
                                )
                        )
                )
                .build();

        SearchHits<OrderAnalysis> hits =
                elasticsearchOperations.search(query, OrderAnalysis.class);

        return extractOrderCountByStatus(hits, "order_by_month");
    }

    private Map<String, Map<Status, Long>> extractOrderCountByStatus(
            SearchHits<?> hits,
            String histogramName
    ) {

        Map<String, Map<Status, Long>> result = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
        List<ElasticsearchAggregation> aggs =
                (List<ElasticsearchAggregation>) hits.getAggregations().aggregations();

        ElasticsearchAggregation wrapper = aggs.stream()
                .filter(a -> histogramName.equals(a.aggregation().getName()))
                .findFirst()
                .orElse(null);

        if (wrapper == null) return result;

        DateHistogramAggregate histogram =
                wrapper.aggregation().getAggregate().dateHistogram();

        for (DateHistogramBucket bucket : histogram.buckets().array()) {

            StringTermsAggregate statusAgg =
                    bucket.aggregations().get("by_status").sterms();

            Map<Status, Long> statusMap = new EnumMap<>(Status.class);

            for (StringTermsBucket tb : statusAgg.buckets().array()) {
                Status status = Status.valueOf(tb.key().stringValue());
                statusMap.put(status, tb.docCount());
            }

            statusMap.putIfAbsent(Status.COMPLETED, 0L);
            statusMap.putIfAbsent(Status.CANCELLED, 0L);

            result.put(bucket.keyAsString(), statusMap);
        }

        return result;
    }
    private List<OrderStatusChartPoint> toOrderStatusChartPoints(
            Map<String, Map<Status, Long>> aggregationMap
    ) {


        List<OrderStatusChartPoint> result = new ArrayList<>();

        aggregationMap.forEach((label, statusMap) -> {
            result.add(
                    OrderStatusChartPoint.builder()
                            .label(label)
                            .completed(statusMap.get(Status.COMPLETED))
                            .cancelled(statusMap.get(Status.CANCELLED))
                            .build()
            );
        });

        return result;
    }

    public List<StockProductTable> getTop10BestSellingProducts() {

        Map<String, Long> soldMap = getTop10BestSellingSkus();

        Set<String> skus = soldMap.keySet();

        Map<String, BigDecimal> revenueMap =
                getRevenueBySku(skus);

        Map<String, ProductInfoResponse> infoMap =
                getProductInfo(skus);

        List<StockProductTable> result = new ArrayList<>();

        soldMap.forEach((sku, soldQty) -> {

            ProductInfoResponse info = infoMap.get(sku);

            result.add(
                    StockProductTable.builder()
                            .sku(sku)
                            .variantName(info != null ? info.getVariantName() : null)
                            .thumbnail(info != null ? info.getThumbnail() : null)
                            .totalSold(soldQty)
                            .totalRevenue(
                                    revenueMap.getOrDefault(sku, BigDecimal.ZERO)
                            )
                            .build()
            );
        });

        return result;
    }

    public List<StockProductTable> getTop10SlowSellingProducts() {

        Map<String, Long> soldMap = getTop10SlowSellingSku();

        Set<String> skus = soldMap.keySet();

        Map<String, BigDecimal> revenueMap =
                getRevenueBySku(skus);

        Map<String, ProductInfoResponse> infoMap =
                getProductInfo(skus);

        List<StockProductTable> result = new ArrayList<>();

        soldMap.forEach((sku, soldQty) -> {

            ProductInfoResponse info = infoMap.get(sku);

            result.add(
                    StockProductTable.builder()
                            .sku(sku)
                            .variantName(info != null ? info.getVariantName() : null)
                            .thumbnail(info != null ? info.getThumbnail() : null)
                            .totalSold(soldQty)
                            .totalRevenue(
                                    revenueMap.getOrDefault(sku, BigDecimal.ZERO)
                            )
                            .build()
            );
        });

        return result;
    }

    public Map<String, Long> getTop10BestSellingSkus() {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("transactionType")
                        .value("OUT")
                ))
                .withAggregation("by_sku",
                        Aggregation.of(a -> a
                                .terms(t -> t
                                        .field("sku")
                                        .size(1000)
                                )
                                .aggregations("total_out",
                                        Aggregation.of(sa ->
                                                sa.sum(s -> s.field("quantity"))
                                        )
                                )
                                .aggregations("sku_sort",
                                        Aggregation.of(pa ->
                                                pa.bucketSort(bs -> bs
                                                        .sort(s -> s
                                                                .field(f -> f
                                                                        .field("total_out")
                                                                        .order(SortOrder.Desc)
                                                                )
                                                        )
                                                        .size(10)
                                                )
                                        )
                                )
                        )
                )
                .build();

        SearchHits<InventoryTransactionAnalysis> hits =
                elasticsearchOperations.search(query, InventoryTransactionAnalysis.class);

        Map<String, Long> result = new LinkedHashMap<>();

        AggregationsContainer<?> container = hits.getAggregations();
        if (container == null) return result;

        StringTermsAggregate terms =
                ((List<ElasticsearchAggregation>) container.aggregations())
                        .get(0)
                        .aggregation()
                        .getAggregate()
                        .sterms();

        for (StringTermsBucket b : terms.buckets().array()) {
            long totalOut =
                    (long) b.aggregations()
                            .get("total_out")
                            .sum()
                            .value();
            result.put(b.key().stringValue(), totalOut);
        }

        return result;
    }

    public Map<String, Long> getTop10SlowSellingSku() {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                        .field("transactionType")
                        .value("OUT")
                ))
                .withAggregation("by_sku",
                        Aggregation.of(a -> a
                                .terms(t -> t
                                        .field("sku")
                                        .size(1000)
                                )
                                .aggregations("total_out",
                                        Aggregation.of(sa ->
                                                sa.sum(s -> s.field("quantity"))
                                        )
                                )
                                .aggregations("sku_sort",
                                        Aggregation.of(pa ->
                                                pa.bucketSort(bs -> bs
                                                        .sort(s -> s
                                                                .field(f -> f
                                                                        .field("total_out")
                                                                        .order(SortOrder.Asc)
                                                                )
                                                        )
                                                        .size(10)
                                                )
                                        )
                                )
                        )
                )
                .build();

        SearchHits<InventoryTransactionAnalysis> hits =
                elasticsearchOperations.search(query, InventoryTransactionAnalysis.class);

        Map<String, Long> result = new LinkedHashMap<>();

        AggregationsContainer<?> container = hits.getAggregations();
        if (container == null) return result;

        List<ElasticsearchAggregation> aggs =
                (List<ElasticsearchAggregation>) container.aggregations();

        StringTermsAggregate terms =
                aggs.get(0).aggregation().getAggregate().sterms();

        for (StringTermsBucket bucket : terms.buckets().array()) {

            long totalOut =
                    (long) bucket.aggregations()
                            .get("total_out")
                            .sum()
                            .value();

            result.put(bucket.key().stringValue(), totalOut);
        }

        return result;
    }

    public Map<String, BigDecimal> getRevenueBySku(Set<String> skus) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.terms(t -> t
                        .field("sku")
                        .terms(v -> v.value(
                                skus.stream().map(FieldValue::of).toList()
                        ))
                ))
                .withAggregation("by_sku",
                        Aggregation.of(a -> a
                                .terms(t -> t.field("sku"))
                                .aggregations("total_revenue",
                                        Aggregation.of(sa -> sa
                                                .sum(s -> s
                                                        .script(sc -> sc
                                                                .source("doc['quantity'].value * doc['sellPrice'].value")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .withMaxResults(100)
                .build();

        SearchHits<OrderItem> hits =
                elasticsearchOperations.search(query, OrderItem.class);

        Map<String, BigDecimal> result = new HashMap<>();

        AggregationsContainer<?> container = hits.getAggregations();
        if (container == null) return result;

        StringTermsAggregate terms =
                ((List<ElasticsearchAggregation>) container.aggregations())
                        .get(0)
                        .aggregation()
                        .getAggregate()
                        .sterms();

        for (StringTermsBucket b : terms.buckets().array()) {
            result.put(
                    b.key().stringValue(),
                    BigDecimal.valueOf(
                            b.aggregations().get("total_revenue").sum().value()
                    )
            );
        }

        return result;
    }
    public Map<String, ProductInfoResponse> getProductInfo(Set<String> skus) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.terms(t -> t
                        .field("sku")
                        .terms(v -> v.value(
                                skus.stream().map(FieldValue::of).toList()
                        ))
                ))
                .build();

        SearchHits<OrderItem> hits =
                elasticsearchOperations.search(query, OrderItem.class);

        Map<String, ProductInfoResponse> result = new HashMap<>();

        hits.forEach(hit -> {
            OrderItem item = hit.getContent();
            result.putIfAbsent(
                    item.getSku(),
                    new ProductInfoResponse(
                            item.getSku(),
                            item.getVariantName(),
                            item.getThumbnail()
                    )
            );
        });

        return result;
    }


}


