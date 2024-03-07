package com.elijahwaswa.mobilemoneyservice.service;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.MobileMoneyShortcodeType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import mpesa.MpesaResponse;
import mpesa.util.ResponseParserType;
import mpesa.util.STKTransactionType;
import mpesa.util.TrxCodeType;

public interface MoneyService {
    TransactionDto validateResponse(MpesaResponse mpesaResponse, ResponseParserType responseParserType);

    TransactionDto initStkPush(MobileMoney mobileMoney,MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, double amount, long phoneNumber, String briefDescription);

    TransactionDto checkStkStatus(MobileMoney mobileMoney,MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, String checkoutRequestId);

    TransactionDto registerUrls(MobileMoney mobileMoney, int businessShortCode);

    TransactionDto businessToBusinessPayment(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, long creditPhoneNumber, String remarks);

    TransactionDto businessToBusinessPaymentStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, String creditBusinessName);

    TransactionDto businessToCustomerDisbursement(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, long phoneNumber, double amount, String remarks);

    TransactionDto remitTax(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, String remarks);

    TransactionDto generateQrCode(MobileMoney mobileMoney, TrxCodeType trxCodeType, double amount);
}
