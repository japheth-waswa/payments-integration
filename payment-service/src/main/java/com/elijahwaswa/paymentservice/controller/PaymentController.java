package com.elijahwaswa.paymentservice.controller;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/payments")
@AllArgsConstructor
public class PaymentController {

    private PaymentService paymentService;

    //    @GetMapping("entity-ref")
    @GetMapping
    public ResponseEntity<List<PaymentDto>> getPayments(@RequestParam(required = false) String entityRef,
                                                        @RequestParam(required = false) PaymentMode paymentMode,
                                                        @RequestParam(required = false) PaymentOrganization paymentOrganization,
                                                        @RequestParam(required = false) String organizationRefNumber,
                                                        @RequestParam int pageNumber,
                                                        @RequestParam int pageSize) {
        if (entityRef != null && !entityRef.isBlank() && paymentOrganization != null && organizationRefNumber != null && !organizationRefNumber.isBlank()) {
            return new ResponseEntity<>(paymentService.getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(entityRef, paymentOrganization, organizationRefNumber, pageNumber, pageSize), HttpStatus.OK);
        } else if (entityRef != null && !entityRef.isBlank() && organizationRefNumber != null && !organizationRefNumber.isBlank()) {
            return new ResponseEntity<>(paymentService.getPaymentsByEntityRefAndOrganizationRefNumber(entityRef, organizationRefNumber, pageNumber, pageSize), HttpStatus.OK);
        } else if (entityRef != null && !entityRef.isBlank() && paymentOrganization != null) {
            return new ResponseEntity<>(paymentService.getPaymentsByEntityRefAndPaymentOrganization(entityRef, paymentOrganization, pageNumber, pageSize), HttpStatus.OK);
        } else if (entityRef != null && !entityRef.isBlank() && paymentMode != null) {
            return new ResponseEntity<>(paymentService.getPaymentsByEntityRefAndPaymentMode(entityRef, paymentMode, pageNumber, pageSize), HttpStatus.OK);
        } else if (entityRef != null && !entityRef.isBlank()) {
            return new ResponseEntity<>(paymentService.getPaymentsByEntityRef(entityRef, pageNumber, pageSize), HttpStatus.OK);
        }
        return new ResponseEntity<>(paymentService.getPayments(pageNumber, pageSize), HttpStatus.OK);
    }


}
