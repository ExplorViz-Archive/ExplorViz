/*
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
 */

import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.base.util.*;
import org.ofbiz.accounting.payment.*;
import org.ofbiz.order.shoppingcart.*;
import org.ofbiz.party.contact.*;

cart = session.getAttribute("shoppingCart");
orderPartyId = cart.getPartyId();
currencyUomId = cart.getCurrency();
context.cart = cart;
context.paymentMethodType = request.getParameter("paymentMethodType");

// nuke the event messages
request.removeAttribute("_EVENT_MESSAGE_");

// If there's a paymentMethodId request attribute, the user has just created a new payment method,
//  so put the new paymentMethodId in the context for the UI
newPaymentMethodId=request.getAttribute("paymentMethodId");
if (newPaymentMethodId) {
    context.checkOutPaymentId = newPaymentMethodId;
}

if (orderPartyId && !orderPartyId.equals("_NA_")) {
    orderParty = delegator.findByPrimaryKey("Party", [partyId : orderPartyId]);
    orderPerson = orderParty.getRelatedOne("Person");
    context.orderParty = orderParty;
    context.orderPerson = orderPerson;
    if (orderParty) {
        context.paymentMethodList = EntityUtil.filterByDate(orderParty.getRelated("PaymentMethod"), true);

        billingAccountList = BillingAccountWorker.makePartyBillingAccountList(userLogin, currencyUomId, orderPartyId, delegator, dispatcher);
        if (billingAccountList) {
            context.selectedBillingAccountId = cart.getBillingAccountId();
            context.billingAccountList = billingAccountList;
        }
    }
}

if (request.getParameter("useShipAddr") && cart.getShippingContactMechId()) {
    shippingContactMech = cart.getShippingContactMechId();
    postalAddress = delegator.findByPrimaryKey("PostalAddress", [contactMechId : shippingContactMech]);
    context.postalFields = postalAddress;
} else {
    context.postalFields = UtilHttp.getParameterMap(request);
}

if (cart) {
    if (cart.getPaymentMethodIds()) {
        checkOutPaymentId = cart.getPaymentMethodIds().get(0);
        context.checkOutPaymentId = checkOutPaymentId;
        if (!orderParty) {
            paymentMethod = delegator.findByPrimaryKey("PaymentMethod", [paymentMethodId : checkOutPaymentId]);
            if ("CREDIT_CARD".equals(paymentMethod?.paymentMethodTypeId)) {
                paymentMethodType = "CC";
                account = paymentMethod.getRelatedOne("CreditCard");
                context.creditCard = account;
                context.paymentMethodType = paymentMethodType;
            } else if ("EFT_ACCOUNT".equals(paymentMethod.paymentMethodTypeId)) {
                paymentMethodType = "EFT";
                account = paymentMethod.getRelatedOne("EftAccount");
                context.eftAccount = account;
                context.paymentMethodType = paymentMethodType;
            }
            if (account) {
                address = account.getRelatedOne("PostalAddress");
                context.postalAddress = address;
            }
        }
    } else if (cart.getPaymentMethodTypeIds()) {
        checkOutPaymentId = cart.getPaymentMethodTypeIds().get(0);
        context.checkOutPaymentId = checkOutPaymentId;
    }
}
