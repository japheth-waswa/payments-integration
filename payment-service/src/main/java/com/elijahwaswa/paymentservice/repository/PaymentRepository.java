package com.elijahwaswa.paymentservice.repository;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByEntityRef(String entityRef, Pageable pageable);

    Page<Payment> findAllByEntityRefAndPaymentMode(String entityRef, PaymentMode paymentMode, Pageable pageable);

    Page<Payment> findAllByEntityRefAndPaymentOrganization(String entityRef, PaymentOrganization paymentOrganization, Pageable pageable);

    Page<Payment> findAllByEntityRefAndOrganizationRefNumber(String entityRef, String organizationRefNumber, Pageable pageable);

    Page<Payment> findAllByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(String entityRef, PaymentOrganization paymentOrganization, String organizationRefNumber, Pageable pageable);
    Page<Payment> findAllByOrganizationRefNumber(String organizationRefNumber, Pageable pageable);
    Page<Payment> findAllByPaymentMode(PaymentMode paymentMode, Pageable pageable);
    Page<Payment> findAllByPaymentOrganization(PaymentOrganization paymentOrganization, Pageable pageable);
}
