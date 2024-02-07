package com.elijahwaswa.paymentservice.service;

import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.entity.Payment;

import java.util.List;

public interface IPaymentService {
    PaymentDto savePayment(PaymentDto paymentDto);
    List<PaymentDto> getPayments(int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRef(String entityRef,int pageNumber, int pageSize);
    String computePaymentKey(Payment payment);
}
