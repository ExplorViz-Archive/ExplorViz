/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.accounting.payment;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

/**
 * Services for Payment maintenance
 */
public class PaymentMethodServices {

    public final static String module = PaymentMethodServices.class.getName();
    public final static String resource = "AccountingUiLabels";
    public static final String resourceError = "AccountingUiLabels";

    /**
     * Deletes a PaymentMethod entity according to the parameters passed in the context
     * <b>security check</b>: userLogin partyId must equal paymentMethod partyId, or must have PAY_INFO_DELETE permission
     * @param ctx The DispatchContext that this service is operating in
     * @param context Map containing the input parameters
     * @return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> deletePaymentMethod(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        // never delete a PaymentMethod, just put a to date on the link to the party
        String paymentMethodId = (String) context.get("paymentMethodId");
        GenericValue paymentMethod = null;

        try {
            paymentMethod = delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingPaymentMethodCannotBeDeleted",
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        if (paymentMethod == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingPaymentMethodCannotBeDeleted",
                    UtilMisc.toMap("errorString", ""), locale));
        }

        // <b>security check</b>: userLogin partyId must equal paymentMethod partyId, or must have PAY_INFO_DELETE permission
        if (paymentMethod.get("partyId") == null || !paymentMethod.getString("partyId").equals(userLogin.getString("partyId"))) {
            if (!security.hasEntityPermission("PAY_INFO", "_DELETE", userLogin)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                        "AccountingPaymentMethodNoPermissionToDelete", locale));
            }
        }

        paymentMethod.set("thruDate", now);
        try {
            paymentMethod.store();
        } catch (GenericEntityException e) {
            Debug.logWarning(e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingPaymentMethodCannotBeDeletedWriteFailure", 
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    public static Map<String, Object> makeExpireDate(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        String expMonth = (String) context.get("expMonth");
        String expYear = (String) context.get("expYear");

        StringBuilder expDate = new StringBuilder();
        expDate.append(expMonth);
        expDate.append("/");
        expDate.append(expYear);
        result.put("expireDate", expDate.toString());
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /**
     * Creates CreditCard and PaymentMethod entities according to the parameters passed in the context
     * <b>security check</b>: userLogin partyId must equal partyId, or must have PAY_INFO_CREATE permission
     * @param ctx The DispatchContext that this service is operating in
     * @param context Map containing the input parameters
     * @return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> createCreditCard(DispatchContext ctx, Map<String, Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_CREATE");

        if (result.size() > 0) return result;

        // do some more complicated/critical validation...
        List<String> messages = FastList.newInstance();

        // first remove all spaces from the credit card number
        context.put("cardNumber", StringUtil.removeSpaces((String) context.get("cardNumber")));
        if (!UtilValidate.isCardMatch((String) context.get("cardType"), (String) context.get("cardNumber"))) {
            messages.add(
                UtilProperties.getMessage(resource, "AccountingCreditCardNumberInvalid",
                    UtilMisc.toMap("cardType", (String) context.get("cardType"),
                                   "validCardType", UtilValidate.getCardType((String) context.get("cardNumber"))), locale));
        }

        if (!UtilValidate.isDateAfterToday((String) context.get("expireDate"))) {
            messages.add(
                UtilProperties.getMessage(resource, "AccountingCreditCardExpireDateBeforeToday",
                    UtilMisc.toMap("expireDate", (String) context.get("expireDate")), locale));
        }

        if (messages.size() > 0) {
            return ServiceUtil.returnError(messages);
        }

        List<GenericValue> toBeStored = FastList.newInstance();
        GenericValue newPm = delegator.makeValue("PaymentMethod");

        toBeStored.add(newPm);
        GenericValue newCc = delegator.makeValue("CreditCard");

        toBeStored.add(newCc);

        String newPmId = (String) context.get("paymentMethodId");
        if (UtilValidate.isEmpty(newPmId)) {
            try {
                newPmId = delegator.getNextSeqId("PaymentMethod");
            } catch (IllegalArgumentException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "AccountingCreditCardCreateIdGenerationFailure", locale));
            }
        }

        newPm.set("partyId", partyId);
        newPm.set("description",context.get("description"));
        newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
        newPm.set("thruDate", context.get("thruDate"));
        newCc.set("companyNameOnCard", context.get("companyNameOnCard"));
        newCc.set("titleOnCard", context.get("titleOnCard"));
        newCc.set("firstNameOnCard", context.get("firstNameOnCard"));
        newCc.set("middleNameOnCard", context.get("middleNameOnCard"));
        newCc.set("lastNameOnCard", context.get("lastNameOnCard"));
        newCc.set("suffixOnCard", context.get("suffixOnCard"));
        newCc.set("cardType", context.get("cardType"));
        newCc.set("cardNumber", context.get("cardNumber"));
        newCc.set("expireDate", context.get("expireDate"));

        newPm.set("paymentMethodId", newPmId);
        newPm.set("paymentMethodTypeId", "CREDIT_CARD");
        newCc.set("paymentMethodId", newPmId);

        GenericValue newPartyContactMechPurpose = null;
        String contactMechId = (String) context.get("contactMechId");

        if (UtilValidate.isNotEmpty(contactMechId) && !contactMechId.equals("_NEW_")) {
            // set the contactMechId on the credit card
            newCc.set("contactMechId", context.get("contactMechId"));
            // add a PartyContactMechPurpose of BILLING_LOCATION if necessary
            String contactMechPurposeTypeId = "BILLING_LOCATION";

            GenericValue tempVal = null;

            try {
                List<GenericValue> allPCMPs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, "contactMechPurposeTypeId", contactMechPurposeTypeId), null), true);

                tempVal = EntityUtil.getFirst(allPCMPs);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                tempVal = null;
            }

            if (tempVal == null) {
                // no value found, create a new one
                newPartyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, "contactMechPurposeTypeId", contactMechPurposeTypeId, "fromDate", now));
            }
        }

        if (newPartyContactMechPurpose != null) toBeStored.add(newPartyContactMechPurpose);

        try {
            delegator.storeAll(toBeStored);
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingCreditCardCreateWriteFailure", locale) + e.getMessage());
        }

        result.put("paymentMethodId", newCc.getString("paymentMethodId"));
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /**
     * Updates CreditCard and PaymentMethod entities according to the parameters passed in the context
     * <b>security check</b>: userLogin partyId must equal partyId, or must have PAY_INFO_UPDATE permission
     * @param ctx The DispatchContext that this service is operating in
     * @param context Map containing the input parameters
     * @return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> updateCreditCard(DispatchContext ctx, Map<String, Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_UPDATE");

        if (result.size() > 0) return result;

        List<GenericValue> toBeStored = FastList.newInstance();
        boolean isModified = false;

        GenericValue paymentMethod = null;
        GenericValue newPm = null;
        GenericValue creditCard = null;
        GenericValue newCc = null;
        String paymentMethodId = (String) context.get("paymentMethodId");

        try {
            creditCard = delegator.findByPrimaryKey("CreditCard", UtilMisc.toMap("paymentMethodId", paymentMethodId));
            paymentMethod = delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "AccountingCreditCardUpdateReadFailure", locale) + e.getMessage());
        }

        if (creditCard == null || paymentMethod == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "AccountingCreditCardUpdateWithPaymentMethodId", locale) + paymentMethodId);
        }
        if (!paymentMethod.getString("partyId").equals(partyId) && !security.hasEntityPermission("PAY_INFO", "_UPDATE", userLogin)) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "AccountingCreditCardUpdateWithoutPermission", UtilMisc.toMap("partyId", partyId, 
                            "paymentMethodId", paymentMethodId), locale));
        }

        // do some more complicated/critical validation...
        List<String> messages = FastList.newInstance();

        // first remove all spaces from the credit card number
        String updatedCardNumber = StringUtil.removeSpaces((String) context.get("cardNumber"));
        if (updatedCardNumber.startsWith("*")) {
            // get the masked card number from the db
            String origCardNumber = creditCard.getString("cardNumber");
            String origMaskedNumber = "";
            int cardLength = origCardNumber.length() - 4;
            for (int i = 0; i < cardLength; i++) {
                origMaskedNumber = origMaskedNumber + "*";
            }
            origMaskedNumber = origMaskedNumber + origCardNumber.substring(cardLength);

            // compare the two masked numbers
            if (updatedCardNumber.equals(origMaskedNumber)) {
                updatedCardNumber = origCardNumber;
            }
        }
        context.put("cardNumber", updatedCardNumber);

        if (!UtilValidate.isCardMatch((String) context.get("cardType"), (String) context.get("cardNumber"))) {
            messages.add(
                UtilProperties.getMessage(resource, "AccountingCreditCardNumberInvalid",
                    UtilMisc.toMap("cardType", (String) context.get("cardType"),
                                   "validCardType", UtilValidate.getCardType((String) context.get("cardNumber"))), locale));
        }

        if (!UtilValidate.isDateAfterToday((String) context.get("expireDate"))) {
            messages.add(
                UtilProperties.getMessage(resource, "AccountingCreditCardExpireDateBeforeToday",
                    UtilMisc.toMap("expireDate", (String) context.get("expireDate")), locale));
        }

        if (messages.size() > 0) {
            return ServiceUtil.returnError(messages);
        }

        newPm = GenericValue.create(paymentMethod);
        toBeStored.add(newPm);
        newCc = GenericValue.create(creditCard);
        toBeStored.add(newCc);

        String newPmId = null;
        try {
            newPmId = delegator.getNextSeqId("PaymentMethod");
        } catch (IllegalArgumentException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "AccountingCreditCardUpdateIdGenerationFailure", locale));

        }

        newPm.set("partyId", partyId);
        newPm.set("fromDate", context.get("fromDate"), false);
        newPm.set("description",context.get("description"));
        // The following check is needed to avoid to reactivate an expired pm
        if (newPm.get("thruDate") == null) {
            newPm.set("thruDate", context.get("thruDate"));
        }
        newCc.set("companyNameOnCard", context.get("companyNameOnCard"));
        newCc.set("titleOnCard", context.get("titleOnCard"));
        newCc.set("firstNameOnCard", context.get("firstNameOnCard"));
        newCc.set("middleNameOnCard", context.get("middleNameOnCard"));
        newCc.set("lastNameOnCard", context.get("lastNameOnCard"));
        newCc.set("suffixOnCard", context.get("suffixOnCard"));

        newCc.set("cardType", context.get("cardType"));
        newCc.set("cardNumber", context.get("cardNumber"));
        newCc.set("expireDate", context.get("expireDate"));

        GenericValue newPartyContactMechPurpose = null;
        String contactMechId = (String) context.get("contactMechId");

        if (UtilValidate.isNotEmpty(contactMechId) && !contactMechId.equals("_NEW_")) {
            // set the contactMechId on the credit card
            newCc.set("contactMechId", contactMechId);
        }

        if (!newCc.equals(creditCard) || !newPm.equals(paymentMethod)) {
            newPm.set("paymentMethodId", newPmId);
            newCc.set("paymentMethodId", newPmId);

            newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
            isModified = true;
        }

        if (UtilValidate.isNotEmpty(contactMechId) && !contactMechId.equals("_NEW_")) {

            // add a PartyContactMechPurpose of BILLING_LOCATION if necessary
            String contactMechPurposeTypeId = "BILLING_LOCATION";

            GenericValue tempVal = null;

            try {
                List<GenericValue> allPCMPs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, "contactMechPurposeTypeId", contactMechPurposeTypeId), null), true);

                tempVal = EntityUtil.getFirst(allPCMPs);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                tempVal = null;
            }

            if (tempVal == null) {
                // no value found, create a new one
                newPartyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, "contactMechPurposeTypeId", contactMechPurposeTypeId, "fromDate", now));
            }
        }

        if (isModified) {
            // Debug.logInfo("yes, is modified", module);
            if (newPartyContactMechPurpose != null) toBeStored.add(newPartyContactMechPurpose);

            // set thru date on old paymentMethod
            paymentMethod.set("thruDate", now);
            toBeStored.add(paymentMethod);

            try {
                delegator.storeAll(toBeStored);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                        "AccountingCreditCardUpdateWriteFailure", locale) + e.getMessage());
            }
        } else {
            result.put("paymentMethodId", paymentMethodId);
            result.put("oldPaymentMethodId", paymentMethodId);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            if (contactMechId == null || !contactMechId.equals("_NEW_")) {
                result.put(ModelService.SUCCESS_MESSAGE, UtilProperties.getMessage(resource, 
                        "AccountingNoChangesMadeNotUpdatingCreditCard", locale));
            }

            return result;
        }

        result.put("oldPaymentMethodId", paymentMethodId);
        result.put("paymentMethodId", newCc.getString("paymentMethodId"));

        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    public static Map<String, Object> clearCreditCardData(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String paymentMethodId = (String) context.get("paymentMethodId");

        // get the cc object
        Delegator delegator = dctx.getDelegator();
        GenericValue creditCard;
        try {
            creditCard = delegator.findByPrimaryKey("CreditCard", UtilMisc.toMap("paymentMethodId", paymentMethodId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // clear the info and store it
        creditCard.set("cardNumber", "0000000000000000"); // set so it doesn't blow up in UIs
        creditCard.set("expireDate", "01/1970"); // same here
        try {
            delegator.store(creditCard);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // expire the payment method
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> expireCtx = UtilMisc.<String, Object>toMap("userLogin", userLogin, 
                "paymentMethodId", paymentMethodId);
        Map<String, Object> expireResp;
        try {
            expireResp = dispatcher.runSync("deletePaymentMethod", expireCtx);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (ServiceUtil.isError(expireResp)) {
            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(expireResp));
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> createGiftCard(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_CREATE");

        if (result.size() > 0)
            return result;

        List<GenericValue> toBeStored = FastList.newInstance();
        GenericValue newPm = delegator.makeValue("PaymentMethod");
        toBeStored.add(newPm);
        GenericValue newGc = delegator.makeValue("GiftCard");
        toBeStored.add(newGc);

        String newPmId = (String) context.get("paymentMethodId");
        if (UtilValidate.isEmpty(newPmId)) {
            try {
                newPmId = delegator.getNextSeqId("PaymentMethod");
            } catch (IllegalArgumentException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                        "AccountingGiftCardCannotBeCreated", locale));
            }
        }

        newPm.set("partyId", partyId);
        newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
        newPm.set("thruDate", context.get("thruDate"));
        newPm.set("description",context.get("description"));

        newGc.set("cardNumber", context.get("cardNumber"));
        newGc.set("pinNumber", context.get("pinNumber"));
        newGc.set("expireDate", context.get("expireDate"));

        newPm.set("paymentMethodId", newPmId);
        newPm.set("paymentMethodTypeId", "GIFT_CARD");
        newGc.set("paymentMethodId", newPmId);

        try {
            delegator.storeAll(toBeStored);
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingGiftCardCannotBeCreatedWriteFailure",
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        result.put("paymentMethodId", newGc.getString("paymentMethodId"));
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    public static Map<String, Object> updateGiftCard(DispatchContext ctx, Map<String, Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_UPDATE");

        if (result.size() > 0)
            return result;

        List<GenericValue> toBeStored = FastList.newInstance();
        boolean isModified = false;

        GenericValue paymentMethod = null;
        GenericValue newPm = null;
        GenericValue giftCard = null;
        GenericValue newGc = null;
        String paymentMethodId = (String) context.get("paymentMethodId");

        try {
            giftCard = delegator.findByPrimaryKey("GiftCard", UtilMisc.toMap("paymentMethodId", paymentMethodId));
            paymentMethod = delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingGiftCardCannotBeUpdated",
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        if (giftCard == null || paymentMethod == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingGiftCardCannotBeUpdated",
                    UtilMisc.toMap("errorString", paymentMethodId), locale));
        }
        if (!paymentMethod.getString("partyId").equals(partyId) && !security.hasEntityPermission("PAY_INFO", "_UPDATE", userLogin)) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingGiftCardPartyNotAuthorized",
                    UtilMisc.toMap("partyId", partyId, "paymentMethodId", paymentMethodId), locale));
        }


        // card number (masked)
        String cardNumber = StringUtil.removeSpaces((String) context.get("cardNumber"));
        if (cardNumber.startsWith("*")) {
            // get the masked card number from the db
            String origCardNumber = giftCard.getString("cardNumber");
            StringBuilder origMaskedNumber = new StringBuilder("");
            int cardLength = origCardNumber.length() - 4;
            if (cardLength > 0) {
                for (int i = 0; i < cardLength; i++) {
                    origMaskedNumber.append("*");
                }
                origMaskedNumber.append(origCardNumber.substring(cardLength));
            } else {
                origMaskedNumber.append(origCardNumber);
            }

            // compare the two masked numbers
            if (cardNumber.equals(origMaskedNumber.toString())) {
                cardNumber = origCardNumber;
            }
        }
        context.put("cardNumber", cardNumber);

        newPm = GenericValue.create(paymentMethod);
        toBeStored.add(newPm);
        newGc = GenericValue.create(giftCard);
        toBeStored.add(newGc);

        String newPmId = null;
        try {
            newPmId = delegator.getNextSeqId("PaymentMethod");
        } catch (IllegalArgumentException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingGiftCardCannotBeCreated", locale));
        }

        newPm.set("partyId", partyId);
        newPm.set("fromDate", context.get("fromDate"), false);
        newPm.set("thruDate", context.get("thruDate"));
        newPm.set("description",context.get("description"));

        newGc.set("cardNumber", context.get("cardNumber"));
        newGc.set("pinNumber", context.get("pinNumber"));
        newGc.set("expireDate", context.get("expireDate"));

        if (!newGc.equals(giftCard) || !newPm.equals(paymentMethod)) {
            newPm.set("paymentMethodId", newPmId);
            newGc.set("paymentMethodId", newPmId);

            newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
            isModified = true;
        }

        if (isModified) {
            // set thru date on old paymentMethod
            paymentMethod.set("thruDate", now);
            toBeStored.add(paymentMethod);

            try {
                delegator.storeAll(toBeStored);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                        "AccountingEftAccountCannotBeUpdated",
                        UtilMisc.toMap("errorString", e.getMessage()), locale));
            }
        } else {
            result.put("paymentMethodId", paymentMethodId);
            result.put("oldPaymentMethodId", paymentMethodId);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            result.put(ModelService.SUCCESS_MESSAGE, UtilProperties.getMessage(resource, 
                    "AccountingNoChangesMadeNotUpdatingEftAccount", locale));

            return result;
        }

        result.put("paymentMethodId", newGc.getString("paymentMethodId"));
        result.put("oldPaymentMethodId", paymentMethodId);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /**
     * Creates EftAccount and PaymentMethod entities according to the parameters passed in the context
     * <b>security check</b>: userLogin partyId must equal partyId, or must have PAY_INFO_CREATE permission
     * @param ctx The DispatchContext that this service is operating in
     * @param context Map containing the input parameters
     * @return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> createEftAccount(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_CREATE");

        if (result.size() > 0) return result;

        List<GenericValue> toBeStored = FastList.newInstance();
        GenericValue newPm = delegator.makeValue("PaymentMethod");

        toBeStored.add(newPm);
        GenericValue newEa = delegator.makeValue("EftAccount");

        toBeStored.add(newEa);

        String newPmId = (String) context.get("paymentMethodId");
        if (UtilValidate.isEmpty(newPmId)) {
            try {
                newPmId = delegator.getNextSeqId("PaymentMethod");
            } catch (IllegalArgumentException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                        "AccountingEftAccountCannotBeCreated", locale));
            }
        }

        newPm.set("partyId", partyId);
        newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
        newPm.set("thruDate", context.get("thruDate"));
        newPm.set("description",context.get("description"));
        newEa.set("bankName", context.get("bankName"));
        newEa.set("routingNumber", context.get("routingNumber"));
        newEa.set("accountType", context.get("accountType"));
        newEa.set("accountNumber", context.get("accountNumber"));
        newEa.set("nameOnAccount", context.get("nameOnAccount"));
        newEa.set("companyNameOnAccount", context.get("companyNameOnAccount"));
        newEa.set("contactMechId", context.get("contactMechId"));

        newPm.set("paymentMethodId", newPmId);
        newPm.set("paymentMethodTypeId", "EFT_ACCOUNT");
        newEa.set("paymentMethodId", newPmId);

        GenericValue newPartyContactMechPurpose = null;
        String contactMechId = (String) context.get("contactMechId");

        if (UtilValidate.isNotEmpty(contactMechId)) {
            // add a PartyContactMechPurpose of BILLING_LOCATION if necessary
            String contactMechPurposeTypeId = "BILLING_LOCATION";

            GenericValue tempVal = null;
            try {
                List<GenericValue> allPCMPs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMechPurpose", 
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, 
                                "contactMechPurposeTypeId", contactMechPurposeTypeId), null), true);

                tempVal = EntityUtil.getFirst(allPCMPs);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                tempVal = null;
            }

            if (tempVal == null) {
                // no value found, create a new one
                newPartyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose",
                    UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, 
                            "contactMechPurposeTypeId", contactMechPurposeTypeId, "fromDate", now));
            }
        }

        if (newPartyContactMechPurpose != null)
            toBeStored.add(newPartyContactMechPurpose);

        try {
            delegator.storeAll(toBeStored);
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingEftAccountCannotBeCreatedWriteFailure",
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        result.put("paymentMethodId", newEa.getString("paymentMethodId"));
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /**
     * Updates EftAccount and PaymentMethod entities according to the parameters passed in the context
     * <b>security check</b>: userLogin partyId must equal partyId, or must have PAY_INFO_UPDATE permission
     * @param ctx The DispatchContext that this service is operating in
     * @param context Map containing the input parameters
     * @return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> updateEftAccount(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        Timestamp now = UtilDateTime.nowTimestamp();

        String partyId = ServiceUtil.getPartyIdCheckSecurity(userLogin, security, context, result, "PAY_INFO", "_UPDATE");

        if (result.size() > 0) return result;

        List<GenericValue> toBeStored = FastList.newInstance();
        boolean isModified = false;

        GenericValue paymentMethod = null;
        GenericValue newPm = null;
        GenericValue eftAccount = null;
        GenericValue newEa = null;
        String paymentMethodId = (String) context.get("paymentMethodId");

        try {
            eftAccount = delegator.findByPrimaryKey("EftAccount", UtilMisc.toMap("paymentMethodId", paymentMethodId));
            paymentMethod =
                delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingEftAccountCannotBeUpdatedReadFailure",
                    UtilMisc.toMap("errorString", e.getMessage()), locale));
        }

        if (eftAccount == null || paymentMethod == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingEftAccountCannotBeUpdated",
                    UtilMisc.toMap("errorString", paymentMethodId), locale));
        }
        if (!paymentMethod.getString("partyId").equals(partyId) && !security.hasEntityPermission("PAY_INFO", "_UPDATE", userLogin)) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingEftAccountCannotBeUpdated",
                    UtilMisc.toMap("partyId", partyId, "paymentMethodId", paymentMethodId), locale));
        }

        newPm = GenericValue.create(paymentMethod);
        toBeStored.add(newPm);
        newEa = GenericValue.create(eftAccount);
        toBeStored.add(newEa);

        String newPmId = null;
        try {
            newPmId = delegator.getNextSeqId("PaymentMethod");
        } catch (IllegalArgumentException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                    "AccountingEftAccountCannotBeCreated", locale));
        }

        newPm.set("partyId", partyId);
        newPm.set("fromDate", context.get("fromDate"), false);
        newPm.set("thruDate", context.get("thruDate"));
        newPm.set("description",context.get("description"));
        newEa.set("bankName", context.get("bankName"));
        newEa.set("routingNumber", context.get("routingNumber"));
        newEa.set("accountType", context.get("accountType"));
        newEa.set("accountNumber", context.get("accountNumber"));
        newEa.set("nameOnAccount", context.get("nameOnAccount"));
        newEa.set("companyNameOnAccount", context.get("companyNameOnAccount"));
        newEa.set("contactMechId", context.get("contactMechId"));

        if (!newEa.equals(eftAccount) || !newPm.equals(paymentMethod)) {
            newPm.set("paymentMethodId", newPmId);
            newEa.set("paymentMethodId", newPmId);
            newPm.set("fromDate", (context.get("fromDate") != null ? context.get("fromDate") : now));
            isModified = true;
        }

        GenericValue newPartyContactMechPurpose = null;
        String contactMechId = (String) context.get("contactMechId");

        if (UtilValidate.isNotEmpty(contactMechId)) {
            // add a PartyContactMechPurpose of BILLING_LOCATION if necessary
            String contactMechPurposeTypeId = "BILLING_LOCATION";

            GenericValue tempVal = null;

            try {
                List<GenericValue> allPCMPs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, 
                                "contactMechPurposeTypeId",contactMechPurposeTypeId), null), true);
                tempVal = EntityUtil.getFirst(allPCMPs);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                tempVal = null;
            }

            if (tempVal == null) {
                // no value found, create a new one
                newPartyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose",
                        UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId, 
                                "contactMechPurposeTypeId", contactMechPurposeTypeId, "fromDate", now));
            }
        }

        if (isModified) {
            // Debug.logInfo("yes, is modified", module);
            if (newPartyContactMechPurpose != null)
                toBeStored.add(newPartyContactMechPurpose);

            // set thru date on old paymentMethod
            paymentMethod.set("thruDate", now);
            toBeStored.add(paymentMethod);

            try {
                delegator.storeAll(toBeStored);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, 
                        "AccountingEftAccountCannotBeUpdated",
                        UtilMisc.toMap("errorString", e.getMessage()), locale));
            }
        } else {
            result.put("paymentMethodId", paymentMethodId);
            result.put("oldPaymentMethodId", paymentMethodId);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            result.put(ModelService.SUCCESS_MESSAGE, UtilProperties.getMessage(resource,
                    "AccountingNoChangesMadeNotUpdatingEftAccount", locale));

            return result;
        }

        result.put("paymentMethodId", newEa.getString("paymentMethodId"));
        result.put("oldPaymentMethodId", paymentMethodId);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }
}
