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

import java.lang.*;
import java.math.BigDecimal;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.accounting.payment.*;
import org.ofbiz.order.order.*;
import org.ofbiz.party.contact.*;
import org.ofbiz.product.catalog.*;
import org.ofbiz.order.shoppingcart.*;
import org.ofbiz.product.store.*;
import org.ofbiz.party.party.PartyWorker;

cart = ShoppingCartEvents.getCartObject(request);
context.cart = cart;
context.currencyUomId = cart.getCurrency();
context.productStore = ProductStoreWorker.getProductStore(request);

// nuke the event messages
request.removeAttribute("_EVENT_MESSAGE_");

orderItems = cart.makeOrderItems();
context.orderItems = orderItems;

orderAdjustments = cart.makeAllAdjustments();

orderItemShipGroupInfo = cart.makeAllShipGroupInfos();
if (orderItemShipGroupInfo) {
    orderItemShipGroupInfo.each { osiInfo ->
        if ("OrderAdjustment".equals(osiInfo.getEntityName())) {
            // shipping / tax adjustment(s)
            orderAdjustments.add(osiInfo);
        }
    }
}
context.orderAdjustments = orderAdjustments;

workEfforts = cart.makeWorkEfforts();
context.workEfforts = workEfforts;

orderHeaderAdjustments = OrderReadHelper.getOrderHeaderAdjustments(orderAdjustments, null);
context.orderHeaderAdjustments = orderHeaderAdjustments;
context.headerAdjustmentsToShow = OrderReadHelper.filterOrderAdjustments(orderHeaderAdjustments, true, false, false, false, false);

orderSubTotal = OrderReadHelper.getOrderItemsSubTotal(orderItems, orderAdjustments);
context.orderSubTotal = orderSubTotal;
context.placingCustomerPerson = userLogin?.getRelatedOne("Person");
context.shippingAddress = cart.getShippingAddress();

paymentMethods = cart.getPaymentMethods();
paymentMethod = null;
if (paymentMethods) {
    paymentMethod = paymentMethods.get(0);
    context.paymentMethod = paymentMethod;
}

if ("CREDIT_CARD".equals(paymentMethod?.paymentMethodTypeId)) {
    creditCard = paymentMethod.getRelatedOneCache("CreditCard");
    context.creditCard = creditCard;
    context.formattedCardNumber = ContactHelper.formatCreditCard(creditCard);
} else if ("EFT_ACCOUNT".equals(paymentMethod?.paymentMethodTypeId)) {
    eftAccount = paymentMethod.getRelatedOneCache("EftAccount");
    context.eftAccount = eftAccount;
}

paymentMethodTypeIds = cart.getPaymentMethodTypeIds();
paymentMethodType = null;
paymentMethodTypeId = null;
if (paymentMethodTypeIds) {
    paymentMethodTypeId = paymentMethodTypeIds.get(0);
    paymentMethodType = delegator.findByPrimaryKey("PaymentMethodType", [paymentMethodTypeId : paymentMethodTypeId]);
    context.paymentMethodType = paymentMethodType;
}

webSiteId = CatalogWorker.getWebSiteId(request);
productStoreId = ProductStoreWorker.getProductStoreId(request);
productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
if (productStore) {
    payToPartyId = productStore.payToPartyId;
    paymentAddress =  PaymentWorker.getPaymentAddress(delegator, payToPartyId);
    if (paymentAddress) context.paymentAddress = paymentAddress;
}

billingAddress = null;
if (paymentMethod) {
    creditCard = paymentMethod.getRelatedOne("CreditCard");
    billingAddress = creditCard?.getRelatedOne("PostalAddress");
}
if (billingAddress) context.billingAddress = billingAddress;

billingAccount = cart.getBillingAccountId() ? delegator.findByPrimaryKey("BillingAccount", [billingAccountId : cart.getBillingAccountId()]) : null;
if (billingAccount) context.billingAccount = billingAccount;

context.customerPoNumber = cart.getPoNumber();
context.carrierPartyId = cart.getCarrierPartyId();
context.shipmentMethodTypeId = cart.getShipmentMethodTypeId();
context.shippingInstructions = cart.getShippingInstructions();
context.maySplit = cart.getMaySplit();
context.giftMessage = cart.getGiftMessage();
context.isGift = cart.getIsGift();
context.shipBeforeDate = cart.getShipBeforeDate();
context.shipAfterDate = cart.getShipAfterDate();

shipmentMethodType = delegator.findByPrimaryKey("ShipmentMethodType", [shipmentMethodTypeId : cart.getShipmentMethodTypeId()]);
if (shipmentMethodType) context.shipMethDescription = shipmentMethodType.description;

orh = new OrderReadHelper(orderAdjustments, orderItems);
context.localOrderReadHelper = orh;

shippingAmount = OrderReadHelper.getAllOrderItemsAdjustmentsTotal(orderItems, orderAdjustments, false, false, true);
shippingAmount = shippingAmount.add(OrderReadHelper.calcOrderAdjustments(orderHeaderAdjustments, orderSubTotal, false, false, true));
context.orderShippingTotal = shippingAmount;

taxAmount = OrderReadHelper.getOrderTaxByTaxAuthGeoAndParty(orderAdjustments).taxGrandTotal;
context.orderTaxTotal = taxAmount;
context.orderGrandTotal = OrderReadHelper.getOrderGrandTotal(orderItems, orderAdjustments);

orderName = cart.getOrderName();
context.orderName = orderName;

orderPartyId = cart.getPartyId();
if (orderPartyId) {
    partyMap = PartyWorker.getPartyOtherValues(request, orderPartyId, "orderParty", "orderPerson", "orderPartyGroup");
    if (partyMap) {
        partyMap.each { key, value ->
            context[key] = value;
        }
    }
}

orderTerms = cart.getOrderTerms();
if (orderTerms) {
   context.orderTerms = orderTerms;
}

orderType = cart.getOrderType();
context.orderType = orderType;
