package com.elijahwaswa.paymentservice.dto;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentDto {
    private long id;
    private String organizationRefNumber;
    private PaymentMode paymentMode;
    private PaymentOrganization paymentOrganization;
    private String entityRef;//admission number, account number/id
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private LocalDateTime receivedDate;
}
