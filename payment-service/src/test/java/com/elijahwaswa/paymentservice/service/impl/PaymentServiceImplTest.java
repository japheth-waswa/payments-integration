package com.elijahwaswa.paymentservice.service.impl;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.entity.Payment;
import com.elijahwaswa.paymentservice.exception.ResourceNotFoundException;
import com.elijahwaswa.paymentservice.service.IPaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private IPaymentService paymentService;
    private PaymentDto paymentDto1, paymentDto2, paymentDto3;
    private Payment payment1;

    @BeforeEach
    void setUp() {

        //truncate h2db
        truncateH2DB();

        payment1 = new Payment();
        payment1.setOrganizationRefNumber("TVB");
        payment1.setPaymentMode(PaymentMode.BANK);
        payment1.setPaymentOrganization(PaymentOrganization.KCB);
        payment1.setEntityRef("DSU");
        payment1.setAmount(new BigDecimal("349.12"));
        payment1.setPaymentDate(LocalDateTime.now());
        payment1.setReceivedDate(LocalDateTime.now());

        paymentDto1 = new PaymentDto();
        paymentDto1.setOrganizationRefNumber("xcv");
        paymentDto1.setPaymentMode(PaymentMode.BANK);
        paymentDto1.setPaymentOrganization(PaymentOrganization.COOP);
        paymentDto1.setEntityRef("TY8USO20F9M1");
        paymentDto1.setAmount(new BigDecimal("45902.23"));
        paymentDto1.setPaymentDate(LocalDateTime.now());
        paymentDto1.setReceivedDate(LocalDateTime.now());

        paymentDto2 = new PaymentDto();
        paymentDto2.setOrganizationRefNumber("TYR");
        paymentDto2.setPaymentMode(PaymentMode.MOBILE_MONEY);
        paymentDto2.setPaymentOrganization(PaymentOrganization.MPESA);
        paymentDto2.setEntityRef("TY8USO20F9M1");
        paymentDto2.setAmount(new BigDecimal("5983.23"));
        paymentDto2.setPaymentDate(LocalDateTime.now());
        paymentDto2.setReceivedDate(LocalDateTime.now());

        paymentDto3 = new PaymentDto();
        paymentDto3.setOrganizationRefNumber("Y7ISG");
        paymentDto3.setPaymentMode(PaymentMode.BANK);
        paymentDto3.setPaymentOrganization(PaymentOrganization.KCB);
        paymentDto3.setEntityRef("XU7SSF");
        paymentDto3.setAmount(new BigDecimal("903.23"));
        paymentDto3.setPaymentDate(LocalDateTime.now());
        paymentDto3.setReceivedDate(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
    }

    void truncateH2DB() {
        //truncate or delete data from all tables
        String[] tables = {"payments"};
        for (String table : tables) {
            String sql = "TRUNCATE TABLE " + table;
            jdbcTemplate.execute(sql);
        }
    }

    @Test
    void savePayment_unique_org_ref_num() {
        PaymentDto savedPayment = paymentService.savePayment(paymentDto1);
        assertEquals(savedPayment.getOrganizationRefNumber(), paymentDto1.getOrganizationRefNumber());
    }

    @Test
    void savePayment_same_org_ref_num() {
        //initial save
        paymentService.savePayment(paymentDto1);
        //save the same
        assertThrows(Exception.class, () -> paymentService.savePayment(paymentDto1));
    }

    @Test
    void getPayments_single_range_exists() {
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);
        paymentService.savePayment(paymentDto3);

        List<PaymentDto> payments = paymentService.getPayments(1, 1);

        assertEquals(1, payments.size());
        assertEquals(paymentDto2.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPayments_multiple_range_exists() {
        paymentService.savePayment(paymentDto2);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);

        List<PaymentDto> payments = paymentService.getPayments(0, 3);
        System.out.println(payments);

        assertEquals(3, payments.size());
        assertEquals(paymentDto1.getOrganizationRefNumber(), payments.get(2).getOrganizationRefNumber());
    }

    @Test
    void getPayments_range_does_not_exists() {
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPayments(7, 10));
    }


    @Test
    void getPaymentsByEntityRef_exists() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByEntityRef(paymentDto2.getEntityRef(), 0, 10);

        assertEquals(2, payments.size());
        assertEquals(paymentDto2.getOrganizationRefNumber(), payments.get(1).getOrganizationRefNumber());
    }

    @Test
    void getPaymentByEntityRef_does_not_exists() {
        //retrieve
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByEntityRef("XYZ", 0, 10));
    }

    @Test
    void getPaymentsByEntityRefAndPaymentMode_exists() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByEntityRefAndPaymentMode(paymentDto3.getEntityRef(), paymentDto3.getPaymentMode(), 0, 10);
        assertEquals(1, payments.size());
        assertEquals(paymentDto3.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentMode_does_not_exist() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByEntityRefAndPaymentMode(paymentDto3.getEntityRef(), paymentDto2.getPaymentMode(), 0, 10));
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganization_exists() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByEntityRefAndPaymentOrganization(paymentDto2.getEntityRef(), paymentDto2.getPaymentOrganization(), 0, 10);
        assertEquals(1, payments.size());
        assertEquals(paymentDto2.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganization_does_not_exist() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByEntityRefAndPaymentOrganization(paymentDto2.getEntityRef(), paymentDto3.getPaymentOrganization(), 0, 10));
    }

    @Test
    void getPaymentsByEntityRefAndOrganizationRefNumber_exists() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByEntityRefAndOrganizationRefNumber(paymentDto3.getEntityRef(), paymentDto3.getOrganizationRefNumber(), 0, 10);
        assertEquals(1, payments.size());
        assertEquals(paymentDto3.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByEntityRefAndOrganizationRefNumber_does_not_exist() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);
        paymentService.savePayment(paymentDto3);

        //retrieve
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByEntityRefAndOrganizationRefNumber(paymentDto2.getEntityRef(), paymentDto3.getOrganizationRefNumber(), 0, 10));
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber_exists() {
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(paymentDto2.getEntityRef(), paymentDto2.getPaymentOrganization(), paymentDto2.getOrganizationRefNumber(), 0, 10);
        assertEquals(1, payments.size());
        assertEquals(paymentDto2.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber_does_not_exist() {
        //save
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto2);
        paymentService.savePayment(paymentDto1);
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(paymentDto1.getEntityRef(), paymentDto3.getPaymentOrganization(), payment1.getOrganizationRefNumber(), 0, 10));
    }

    @Test
    void getPaymentsByOrganizationRefNumber_exists() {
        //save
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByOrganizationRefNumber(paymentDto1.getOrganizationRefNumber(), 0, 10);
        assertEquals(1, payments.size());
        assertEquals(paymentDto1.getOrganizationRefNumber(), payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByOrganizationRefNumber_does_not_exist() {
        //save
        paymentService.savePayment(paymentDto2);
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentsByOrganizationRefNumber("9876", 0, 10));
    }

    @Test
    void getPaymentsByPaymentMode_exists(){
        //save
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments1  = paymentService.getPaymentsByPaymentMode(paymentDto3.getPaymentMode(),0,10);
        assertEquals(2,payments1.size());

        //retrieve
        List<PaymentDto> payments2  = paymentService.getPaymentsByPaymentMode(paymentDto3.getPaymentMode(),1,1);
        assertEquals(paymentDto1.getOrganizationRefNumber(),payments2.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByPaymentMode_does_not_exist(){
        //save
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);
        assertThrows(ResourceNotFoundException.class,()->paymentService.getPaymentsByPaymentMode(paymentDto2.getPaymentMode(),0,Integer.MAX_VALUE));
    }

    @Test
    void getPaymentsByPaymentOrganization_exists(){
        //save
        paymentService.savePayment(paymentDto3);
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);

        //retrieve
        List<PaymentDto> payments = paymentService.getPaymentsByPaymentOrganization(paymentDto1.getPaymentOrganization(),0,Integer.MAX_VALUE);
        assertEquals(1,payments.size());
        assertEquals(paymentDto1.getOrganizationRefNumber(),payments.get(0).getOrganizationRefNumber());
    }

    @Test
    void getPaymentsByPaymentOrganization_does_not_exist(){
        //save
        paymentService.savePayment(paymentDto1);
        paymentService.savePayment(paymentDto2);

        assertThrows(ResourceNotFoundException.class,()->paymentService.getPaymentsByPaymentOrganization(paymentDto3.getPaymentOrganization(),0,10));
    }

    @Test
    void computePaymentKey_matching() {
        assertEquals(payment1.getPaymentOrganization() + "-" + payment1.getOrganizationRefNumber(), paymentService.computePaymentKey(payment1));
    }

    @Test
    void computePaymentKey_not_matching() {
        assertNotEquals(payment1.getPaymentMode() + "-" + payment1.getOrganizationRefNumber(), paymentService.computePaymentKey(payment1));
    }
}