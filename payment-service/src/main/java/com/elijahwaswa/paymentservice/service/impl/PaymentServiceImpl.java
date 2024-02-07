package com.elijahwaswa.paymentservice.service.impl;

import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.basedomains.utils.Helpers;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.entity.Payment;
import com.elijahwaswa.paymentservice.exception.ResourceNotFoundException;
import com.elijahwaswa.paymentservice.repository.PaymentRepository;
import com.elijahwaswa.paymentservice.service.IPaymentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    private PaymentRepository paymentRepository;
    private ModelMapper modelMapper;

    @Override
    public PaymentDto savePayment(PaymentDto paymentDto) {
        //convert PaymentDto to Payment
        Payment payment = modelMapper.map(paymentDto, Payment.class);
        //compute the paymentKey
        payment.setPaymentKey(computePaymentKey(payment));
        //save payment
        Payment savedPayment = paymentRepository.save(payment);
        //map Payment to PaymentDto
        return modelMapper.map(savedPayment, PaymentDto.class);
    }

    @Override
    public List<PaymentDto> getPayments(int pageNumber, int pageSize) {

        Page<Payment> paymentPage = paymentRepository.findAll(Helpers.buildPageable(pageNumber, pageSize));

        if (paymentPage.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.PAYMENTS_NOT_FOUND, String.format("Payments not found with page number[%s] and page size[%s]", pageNumber, pageSize));

        return paymentPage
                .stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .collect(Collectors.toList());
    }


    @Override
    public List<PaymentDto> getPaymentsByEntityRef(String entityRef, int pageNumber, int pageSize) {
        Page<Payment> payments = paymentRepository.findAllByEntityRef(entityRef, Helpers.buildPageable(pageNumber, pageSize));

        if (payments.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.PAYMENTS_NOT_FOUND, String.format("Payments for this entityRef not found with page number[%s] and page size[%s]", pageNumber, pageSize));

        return payments
                .stream()
                .map(payment -> modelMapper.map(payment, PaymentDto.class))
                .collect(Collectors.toList());

    }

    @Override
    public String computePaymentKey(Payment payment) {
        return payment.getPaymentOrganization() + "-" + payment.getOrganizationRefNumber();
    }
}
