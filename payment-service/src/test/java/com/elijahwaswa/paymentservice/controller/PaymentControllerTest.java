package com.elijahwaswa.paymentservice.controller;

import com.elijahwaswa.basedomains.enums.PaymentMode;
import com.elijahwaswa.basedomains.enums.PaymentOrganization;
import com.elijahwaswa.basedomains.exception.ErrorCode;
import com.elijahwaswa.paymentservice.dto.PaymentDto;
import com.elijahwaswa.paymentservice.exception.ResourceNotFoundException;
import com.elijahwaswa.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    private static final String PAYMENTS_BASE_URL = "/api/payments";
    private static final String ENTITY_BASE_URL = "entity-ref";

    private PaymentDto paymentDto1, paymentDto2;

    @BeforeEach
    void setup() {
        paymentDto1 = new PaymentDto();
        paymentDto1.setId(23);
        paymentDto1.setOrganizationRefNumber("xcv");
        paymentDto1.setPaymentMode(PaymentMode.BANK);
        paymentDto1.setPaymentOrganization(PaymentOrganization.COOP);
        paymentDto1.setEntityRef("TY8USO20F9M1");
        paymentDto1.setAmount(new BigDecimal("45902.23"));
        paymentDto1.setPaymentDate(LocalDateTime.now());
        paymentDto1.setReceivedDate(LocalDateTime.now());

        paymentDto2 = new PaymentDto();
        paymentDto2.setId(7);
        paymentDto2.setOrganizationRefNumber("TYR");
        paymentDto2.setPaymentMode(PaymentMode.MOBILE_MONEY);
        paymentDto2.setPaymentOrganization(PaymentOrganization.MPESA);
        paymentDto2.setEntityRef("TY8USO20F9M1");
        paymentDto2.setAmount(new BigDecimal("5983.23"));
        paymentDto2.setPaymentDate(LocalDateTime.now());
        paymentDto2.setReceivedDate(LocalDateTime.now());

    }

    @Test
    void getPayments_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPayments(1, 10))
                .thenReturn(Collections.singletonList(paymentDto1));
        //perform the request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].organizationRefNumber", paymentDto1.getOrganizationRefNumber()).exists());
    }

    @Test
    void getPayments_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageSize", "10"))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void getPayments_404() throws Exception {
        //Mocking the service response
        when(paymentService.getPayments(2, 10))
                .thenThrow(new ResourceNotFoundException(ErrorCode.PAYMENTS_NOT_FOUND, "Payments not found!"));
        //perform the request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getPaymentsByEntityRef_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPaymentsByEntityRef(paymentDto2.getEntityRef(), 0, 10))
                .thenReturn(List.of(paymentDto1, paymentDto2));
        //perform request to assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("entityRef", paymentDto2.getEntityRef()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].organizationRefNumber", paymentDto2.getOrganizationRefNumber()).exists());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentMode_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPaymentsByEntityRefAndPaymentMode(paymentDto2.getEntityRef(), paymentDto2.getPaymentMode(), 0, 10))
                .thenReturn(List.of(paymentDto1, paymentDto2));
        //perform request to assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("entityRef", paymentDto2.getEntityRef())
                        .param("paymentMode", String.valueOf(paymentDto2.getPaymentMode())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].organizationRefNumber", paymentDto2.getOrganizationRefNumber()).exists());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganization_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPaymentsByEntityRefAndPaymentOrganization(paymentDto2.getEntityRef(), paymentDto2.getPaymentOrganization(), 0, 10))
                .thenReturn(List.of(paymentDto1, paymentDto2));
        //perform request to assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("entityRef", paymentDto2.getEntityRef())
                        .param("paymentOrganization", String.valueOf(paymentDto2.getPaymentOrganization())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].organizationRefNumber", paymentDto2.getOrganizationRefNumber()).exists());
    }

    @Test
    void getPaymentsByEntityRefAndOrganizationRefNumber_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPaymentsByEntityRefAndOrganizationRefNumber(paymentDto1.getEntityRef(),paymentDto1.getOrganizationRefNumber(), 0, 10))
                .thenReturn(Collections.singletonList(paymentDto1));
        //perform request to assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("entityRef", paymentDto1.getEntityRef())
                        .param("organizationRefNumber", paymentDto1.getOrganizationRefNumber()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].organizationRefNumber", paymentDto1.getOrganizationRefNumber()).exists());
    }

    @Test
    void getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber_200() throws Exception {
        //Mocking the service response
        when(paymentService.getPaymentsByEntityRefAndPaymentOrganizationAndOrganizationRefNumber(paymentDto1.getEntityRef(), paymentDto1.getPaymentOrganization(), paymentDto1.getOrganizationRefNumber(), 0, 10))
                .thenReturn(List.of(paymentDto1, paymentDto2));
        //perform request to assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENTS_BASE_URL)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("entityRef", paymentDto1.getEntityRef())
                        .param("paymentOrganization", String.valueOf(paymentDto1.getPaymentOrganization()))
                        .param("organizationRefNumber", paymentDto1.getOrganizationRefNumber()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].organizationRefNumber", paymentDto1.getOrganizationRefNumber()).exists());
    }

}