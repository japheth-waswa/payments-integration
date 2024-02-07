package com.elijahwaswa.paymentservice.controller;

import com.elijahwaswa.paymentservice.service.IPaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payments")
@AllArgsConstructor
public class PaymentController {
    private IPaymentService paymentService;

//    public ResponseEntity<PaymentDto> getPayment(){
//
//    }
}
