package com.elijahwaswa.paymentservice.kafka;

import com.elijahwaswa.basedomains.dto.PaymentEvent;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentConsumer {

    private PaymentService paymentService;
    private ModelMapper modelMapper;

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePaymentEvent(PaymentEvent paymentEvent){
        //save the payment record
        paymentService.savePayment(modelMapper.map(paymentEvent, PaymentDto.class));
    }
}
