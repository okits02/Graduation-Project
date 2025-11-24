package com.okits02.payment_service.mapper;

import com.okits02.payment_service.dto.request.PaymentCreationRequest;
import com.okits02.payment_service.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentCreationRequest request);
}
