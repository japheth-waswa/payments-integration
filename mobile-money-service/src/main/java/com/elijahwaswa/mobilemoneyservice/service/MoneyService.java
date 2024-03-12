package com.elijahwaswa.mobilemoneyservice.service;

import com.elijahwaswa.basedomains.enums.MobileMoney;
import com.elijahwaswa.basedomains.enums.MobileMoneyResponseType;
import com.elijahwaswa.basedomains.enums.MobileMoneyShortcodeType;
import com.elijahwaswa.mobilemoneyservice.dto.TransactionDto;
import mpesa.MpesaResponse;
import mpesa.util.*;

public interface MoneyService {
    TransactionDto validateResponse(MpesaResponse mpesaResponse, ResponseParserType responseParserType);

    TransactionDto initStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, STKTransactionType stkTransactionType, double amount, long phoneNumber, String briefDescription);

    TransactionDto checkStkStatus(MobileMoney mobileMoney, String transactionRef);

    TransactionDto registerUrls(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, RegisterURLResponseType registerURLResponseType);

    MpesaResponse parseMobileMoneyResponse(MobileMoneyResponseType mobileMoneyResponseType, MpesaResponse mpesaResponse);

    TransactionDto businessToBusinessPayment(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, long creditPhoneNumber, String remarks);

    TransactionDto businessToBusinessPaymentStkPush(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, int creditBusinessShortCode, String creditBusinessName);

    TransactionDto businessToCustomerDisbursement(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, B2CCommandID b2CCommandId, long phoneNumber, double amount, String remarks);

    TransactionDto remitTax(MobileMoney mobileMoney, MobileMoneyShortcodeType mobileMoneyShortcodeType, double amount, String remarks);

    TransactionDto generateQrCode(MobileMoney mobileMoney, TrxCodeType trxCodeType, double amount);
}
