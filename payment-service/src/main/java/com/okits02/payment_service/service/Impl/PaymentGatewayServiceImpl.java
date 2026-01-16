package com.okits02.payment_service.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.okits02.payment_service.configuration.VnpayConfig;
import com.okits02.payment_service.dto.request.VnPayPaymentInfoRequest;
import com.okits02.payment_service.dto.response.VnpResponseDTO;
import com.okits02.payment_service.enums.PaymentMethod;
import com.okits02.payment_service.enums.PaymentStatus;
import com.okits02.payment_service.enums.TransactionType;
import com.okits02.payment_service.kafka.ChangeStatusOrdersEvent;
import com.okits02.payment_service.model.Payment;
import com.okits02.payment_service.model.PaymentSession;
import com.okits02.payment_service.repository.PaymentRepository;
import com.okits02.payment_service.repository.PaymentSessionRepository;
import com.okits02.payment_service.repository.httpClient.VnPayRefundClient;
import com.okits02.payment_service.service.PaymentGatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.out;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentRepository paymentRepository;
    private final VnPayRefundClient vnPayRefundClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public ResponseEntity<?> createVnPayPayment(Payment payment) throws UnsupportedEncodingException, JsonProcessingException {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        long vnpAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        String vnp_TxnRef = VnpayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnpayConfig.getIpAddress(request);
        String vnp_TmnCode = VnpayConfig.vnp_TmnCode;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VnpayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VnpayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "order");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnpayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnpayConfig.hmacSHA512(VnpayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnpayConfig.vnp_PayUrl + "?" + queryUrl;

        // ================= SAVE SESSION =================
        PaymentSession session = PaymentSession.builder()
                .payment(payment)
                .transactionId(vnp_TxnRef)
                .transactionType(TransactionType.PAYMENT)
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .vnp_PayDate(vnp_CreateDate)
                .providerData(new ObjectMapper().writeValueAsString(vnp_Params))
                .build();

        paymentSessionRepository.save(session);

        return ResponseEntity.ok(
                VnpResponseDTO.builder()
                        .paymentUrl(paymentUrl)
                        .txnRef(vnp_TxnRef)
                        .amount(payment.getAmount())
                        .method(PaymentMethod.VNPAY)
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> refundVnPay(Payment payment, String reason) throws JsonProcessingException {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes()).getRequest();
        PaymentSession paidSession =
                paymentSessionRepository
                        .findTopByPaymentIdAndTransactionTypeAndStatusOrderByCreateAtDesc(
                                payment.getId(),
                                TransactionType.PAYMENT,
                                PaymentStatus.SUCCESS
                        );

        if (paidSession == null) {
            throw new RuntimeException("Original transaction not found");
        }
        String requestId = UUID.randomUUID().toString();
        String refundTxnRef = VnpayConfig.getRandomNumber(8);
        long vnpAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        String ipAddr = VnpayConfig.getIpAddress(request);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = formatter.format(cld.getTime());
        String transactionDate = paidSession.getVnp_PayDate();

        PaymentSession refundSession = PaymentSession.builder()
                .payment(payment)
                .transactionId(refundTxnRef)
                .transactionType(TransactionType.REFUND)
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .build();
        paymentSessionRepository.save(refundSession);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("vnp_RequestId", requestId);
        body.put("vnp_Version", "2.1.0");
        body.put("vnp_Command", "refund");
        body.put("vnp_TmnCode", VnpayConfig.vnp_TmnCode);
        body.put("vnp_TransactionType", "02");
        body.put("vnp_TxnRef", paidSession.getTransactionId());
        body.put("vnp_Amount", String.valueOf(vnpAmount));
        body.put("vnp_TransactionNo", "");
        body.put("vnp_TransactionDate", transactionDate);
        body.put("vnp_CreateBy", "system");
        body.put("vnp_CreateDate", now);
        body.put("vnp_IpAddr", ipAddr);
        body.put("vnp_OrderInfo", reason);

        String hashData = String.join("|",
                requestId,
                "2.1.0",
                "refund",
                VnpayConfig.vnp_TmnCode,
                "02",
                paidSession.getTransactionId(),
                String.valueOf(vnpAmount),
                "",
                transactionDate,
                "system",
                now,
                ipAddr,
                reason
        );

        String secureHash = VnpayConfig.hmacSHA512(
                VnpayConfig.secretKey,
                hashData
        );
        body.put("vnp_SecureHash", secureHash);

        Map<String, String> response = vnPayRefundClient.refund(body);

        refundSession.setProviderData(new ObjectMapper().writeValueAsString(response));

        String responseCode = response.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            refundSession.setStatus(PaymentStatus.SUCCESS);
        } else {
            refundSession.setStatus(PaymentStatus.FAILED);
        }

        paymentSessionRepository.save(refundSession);

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<?> vnPayIPN(HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("call back");
        Map<String, String> fields = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length > 0 && value[0] != null && !value[0].isEmpty()) {
                try {
                    fields.put(
                            URLEncoder.encode(key, StandardCharsets.US_ASCII.toString()),
                            URLEncoder.encode(value[0], StandardCharsets.US_ASCII.toString())
                    );
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = VnpayConfig.hashAllFields(fields);

        if (!signValue.equals(vnp_SecureHash)) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "97",
                    "Message", "Invalid Checksum"
            ));
        }

        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        PaymentSession session = paymentSessionRepository.findByTransactionId(vnp_TxnRef);
        Payment payment = session.getPayment();

        if (session == null) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "01",
                    "Message", "Order not found"
            ));
        }

        String responseCode = request.getParameter("vnp_ResponseCode");

        switch (responseCode){

            case "00" -> {
                session.setStatus(PaymentStatus.SUCCESS);
                payment.setStatus(PaymentStatus.SUCCESS);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.SUCCESS);
            }

            case "24" -> {
                session.setStatus(PaymentStatus.CANCELLED);
                payment.setStatus(PaymentStatus.CANCELLED);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.CANCELLED);
            }

            case "10" -> {
                session.setStatus(PaymentStatus.EXPIRED);
                payment.setStatus(PaymentStatus.EXPIRED);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.FAILED);
            }

            default -> {
                    session.setStatus(PaymentStatus.FAILED);
                payment.setStatus(PaymentStatus.FAILED);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.FAILED);
            }
        }

        paymentSessionRepository.save(session);
        paymentRepository.save(payment);


        return ResponseEntity.ok(Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
        ));
    }

    public void OrderStatusEvent(String paymentId, String orderId, PaymentStatus status){
        ChangeStatusOrdersEvent event = ChangeStatusOrdersEvent.builder()
                .orderId(orderId)
                .paymentId(paymentId)
                .status(status)
                .build();
        kafkaTemplate.send("change-status-order", event).whenComplete(
                (result, ex) ->{
                    if (ex != null)
                    {
                        System.err.println("Failed to send message" + ex.getMessage());
                    } else {
                        System.err.println("send message successfully" + result.getProducerRecord());
                    }
                });
    }
}
