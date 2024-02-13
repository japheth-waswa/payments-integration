package com.elijahwaswa.paymentservice.kafka;

import com.elijahwaswa.basedomains.dto.PaymentEvent;
import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentConsumerTest {

    private Producer<String, Object> producer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @SpyBean
    PaymentConsumer paymentConsumer;

    @Autowired
    private NewTopic topic;

    @Captor
    ArgumentCaptor<PaymentEvent> paymentEventArgumentCaptor;

    PaymentEvent paymentEvent;

    @BeforeEach
    void setup() {

        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new JsonSerializer<>()).createProducer();

        paymentEvent = new PaymentEvent();
        paymentEvent.setOrganizationRefNumber("UYY");
        paymentEvent.setPaymentMode(PaymentMode.BANK);
        paymentEvent.setPaymentOrganization(PaymentOrganization.COOP);
        paymentEvent.setEntityRef("VWS");
        paymentEvent.setAmount(new BigDecimal("9382.23"));
        paymentEvent.setPaymentDate(LocalDateTime.now());
        paymentEvent.setReceivedDate(LocalDateTime.now());
    }

    @AfterAll
    void shutdown() {
        producer.close();
    }

    @Test
    void consume() throws InterruptedException, JsonProcessingException {
        //producer send message to kafka
        producer.send(new ProducerRecord<>(topic.name(), 0, paymentEvent.getOrganizationRefNumber(), paymentEvent));
        producer.flush();

        // Read the message and assert its properties
        verify(paymentConsumer, timeout(5000).times(1))
                .consumePaymentEvent(paymentEventArgumentCaptor.capture());

        PaymentEvent kafkaPaymentEvent = paymentEventArgumentCaptor.getValue();
        assertNotNull(kafkaPaymentEvent);
        assertEquals(paymentEvent.getEntityRef(), kafkaPaymentEvent.getEntityRef());
    }

}