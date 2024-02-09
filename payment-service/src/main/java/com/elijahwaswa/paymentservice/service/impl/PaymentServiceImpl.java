package com.elijahwaswa.paymentservice.service.impl;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.utils.Helpers;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.entity.Payment;
import com.elijahwaswa.paymentservice.exception.ResourceNotFoundException;
import com.elijahwaswa.paymentservice.repository.PaymentRepository;
import com.elijahwaswa.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private PaymentRepository paymentRepository;
    private ModelMapper modelMapper;

    private List<PaymentDto> parsePayments(Page<Payment> payments, String notFoundMessage) {
        if (payments.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.PAYMENTS_NOT_FOUND, notFoundMessage);

        return payments
                .stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto savePayment(PaymentDto paymentDto) {
        Payment payment = modelMapper.map(paymentDto, Payment.class);
        payment.setPaymentKey(computePaymentKey(payment));
        Payment savedPayment = paymentRepository.save(payment);
        return modelMapper.map(savedPayment, PaymentDto.class);
    }

    @Override
    public List<PaymentDto> getPayments(int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAll(Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments not found with page number[%s] and page size[%s]", pageNumber, pageSize));
    }


    @Override
    public List<PaymentDto> getPaymentsByEntityRef(String entityRef, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRef(entityRef, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this entityRef[%s] not found with page number[%s] and page size[%s]", entityRef, pageNumber, pageSize));
    }

    @Override
    public List<PaymentDto> getPaymentsByEntityRefAndPaymentMode(String entityRef, PaymentMode paymentMode, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRefAndPaymentMode(entityRef, paymentMode, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this entityRef[%s] and paymentMode[%s] not found with page number[%s] and page size[%s]", entityRef, paymentMode, pageNumber, pageSize));
    }

    @Override
    public List<PaymentDto> getPaymentsByEntityRefAndPaymentOrganization(String entityRef, PaymentOrganization paymentOrganization, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRefAndPaymentOrganization(entityRef, paymentOrganization, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this entity ref[%s] and payment organization[%s] not found!", entityRef, paymentOrganization));
    }

    @Override
    public List<PaymentDto> getPaymentsByEntityRefAndOrganizationRefNumber(String entityRef, String organizationRefNumber, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRefAndOrganizationRefNumber(entityRef, organizationRefNumber, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this entity ref[%s] and organization ref number[%s] not found!", entityRef, organizationRefNumber));
    }

    @Override
    public List<PaymentDto> getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(String entityRef, PaymentOrganization paymentOrganization, String organizationRefNumber, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(entityRef, paymentOrganization, organizationRefNumber, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this entity ref[%s],payment organization[%s] and organization ref number[%s] not found!", entityRef, paymentOrganization, organizationRefNumber));
    }

    @Override
    public List<PaymentDto> getPaymentsByOrganizationRefNumber(String organizationRefNumber, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByOrganizationRefNumber(organizationRefNumber, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this organization ref number[%s] not found!", organizationRefNumber));
    }

    @Override
    public List<PaymentDto> getPaymentsByPaymentMode(PaymentMode paymentMode, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByPaymentMode(paymentMode, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this payment mode[%s] not found!", paymentMode));
    }

    @Override
    public List<PaymentDto> getPaymentsByPaymentOrganization(PaymentOrganization paymentOrganization, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByPaymentOrganization(paymentOrganization, Helpers.buildPageable(pageNumber, pageSize));
        return parsePayments(payments, String.format("Payments for this organization[%s] not found!", paymentOrganization));
    }

    @Override
    public String computePaymentKey(Payment payment) {
        return payment.getPaymentOrganization() + "-" + payment.getOrganizationRefNumber();
    }
}
