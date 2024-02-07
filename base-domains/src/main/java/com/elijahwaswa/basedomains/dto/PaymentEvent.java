package com.elijahwaswa.basedomains.dto;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String organizationRefNumber;//from organization
    private PaymentMode paymentMode;
    private PaymentOrganization paymentOrganization;
    private String entityRef;//admission number, account number/id
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private LocalDateTime receivedDate;
}
