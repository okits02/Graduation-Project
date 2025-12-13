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
import com.okits02.payment_service.kafka.ChangeStatusOrdersEvent;
import com.okits02.payment_service.model.Payment;
import com.okits02.payment_service.model.PaymentSession;
import com.okits02.payment_service.repository.PaymentRepository;
import com.okits02.payment_service.repository.PaymentSessionRepository;
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
import java.util.*;

import static java.lang.System.out;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public ResponseEntity<?> createVnPayPayment(Payment payment) throws UnsupportedEncodingException, JsonProcessingException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        BigDecimal amount = payment.getAmount();
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String vnp_TxnRef = VnpayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnpayConfig.getIpAddress(request);
        String orderType = "other";
        String vnp_TmnCode = VnpayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VnpayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VnpayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
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
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
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
        String providerDataJson = new ObjectMapper().writeValueAsString(vnp_Params);
        PaymentSession session = PaymentSession.builder()
                .payment(payment)
                .transactionId(vnp_TxnRef)
                .providerData(providerDataJson)
                .method(PaymentMethod.VNPAY)
                .status(payment.getStatus())
                .build();
        paymentSessionRepository.save(session);
        return ResponseEntity.ok(VnpResponseDTO.builder()
                .paymentUrl(paymentUrl)
                .txnRef(vnp_TxnRef)
                .amount(payment.getAmount())
                .method(PaymentMethod.VNPAY)
                .build());
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

            case "09" -> {
                session.setStatus(PaymentStatus.FAILED);
                payment.setStatus(PaymentStatus.FAILED);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.FAILED);
            }

            case "10" -> {
                session.setStatus(PaymentStatus.EXPIRED);
                payment.setStatus(PaymentStatus.EXPIRED);
                OrderStatusEvent(payment.getId(), payment.getOrderId(), PaymentStatus.FAILED);
            }

            case "11", "12" -> {
                session.setStatus(PaymentStatus.FAILED);
                payment.setStatus(PaymentStatus.FAILED);
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
