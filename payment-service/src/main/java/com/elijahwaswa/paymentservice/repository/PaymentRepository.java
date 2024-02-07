package com.elijahwaswa.paymentservice.repository;

import com.elijahwaswa.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Page<Payment> findAllByEntityRef(String entityRef, Pageable pageable);
}
