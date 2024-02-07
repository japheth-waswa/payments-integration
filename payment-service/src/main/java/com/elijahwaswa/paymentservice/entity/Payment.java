package com.elijahwaswa.paymentservice.entity;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String organizationRefNumber;
    private PaymentMode paymentMode;
    private PaymentOrganization paymentOrganization;

    @Column(unique = true)
    private String paymentKey;
    private String entityRef;//admission number, account number/id
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private LocalDateTime receivedDate;
}
