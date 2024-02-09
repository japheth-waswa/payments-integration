package com.elijahwaswa.paymentservice.service;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.entity.Payment;

import java.util.List;

public interface PaymentService {
    PaymentDto savePayment(PaymentDto paymentDto);
    List<PaymentDto> getPayments(int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRef(String entityRef, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRefAndPaymentMode(String entityRef, PaymentMode paymentMode, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRefAndPaymentOrganization(String entityRef, PaymentOrganization paymentOrganization, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRefAndOrganizationRefNumber(String entityRef, String organizationRefNumber, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(String entityRef, PaymentOrganization paymentOrganization, String organizationRefNumber, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByOrganizationRefNumber(String organizationRefNumber, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByPaymentMode(PaymentMode paymentMode, int pageNumber, int pageSize);
    List<PaymentDto> getPaymentsByPaymentOrganization(PaymentOrganization paymentOrganization, int pageNumber, int pageSize);
    String computePaymentKey(Payment payment);
}
