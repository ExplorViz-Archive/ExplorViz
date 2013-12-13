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

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.accounting.payment.*;
import org.ofbiz.party.contact.*;
import org.ofbiz.product.store.*;
import org.ofbiz.order.shoppingcart.shipping.*;

shoppingCart = session.getAttribute("shoppingCart");
currencyUomId = shoppingCart.getCurrency();
partyId = shoppingCart.getPartyId();
party = delegator.findByPrimaryKeyCache("Party", [partyId : partyId]);
productStore = ProductStoreWorker.getProductStore(request);

shippingEstWpr = null;
if (shoppingCart) {
    shippingEstWpr = new ShippingEstimateWrapper(dispatcher, shoppingCart, 0);
    context.shippingEstWpr = shippingEstWpr;
    context.carrierShipmentMethodList = shippingEstWpr.getShippingMethods();
    // Reassign items requiring drop-shipping to new or existing drop-ship groups
    shoppingCart.createDropShipGroups(dispatcher);    
}

profiledefs = delegator.findByPrimaryKey("PartyProfileDefault", [partyId : userLogin.partyId, productStoreId : productStoreId]);
context.profiledefs = profiledefs;

context.shoppingCart = shoppingCart;
context.userLogin = userLogin;
context.productStoreId = productStore.get("productStoreId");
context.productStore = productStore;
shipToParty = delegator.findByPrimaryKeyCache("Party", [partyId : shoppingCart.getShipToCustomerPartyId()]);
context.shippingContactMechList = ContactHelper.getContactMech(shipToParty, "SHIPPING_LOCATION", "POSTAL_ADDRESS", false);
context.emailList = ContactHelper.getContactMechByType(party, "EMAIL_ADDRESS", false);

if (shoppingCart.getShipmentMethodTypeId() && shoppingCart.getCarrierPartyId()) {
    context.chosenShippingMethod = shoppingCart.getShipmentMethodTypeId() + '@' + shoppingCart.getCarrierPartyId();
} else if (profiledefs?.defaultShipMeth) {
    context.chosenShippingMethod = profiledefs.defaultShipMeth;
}

// other profile defaults
if (!shoppingCart.getShippingAddress() && profiledefs?.defaultShipAddr) {
    shoppingCart.setShippingContactMechId(profiledefs.defaultShipAddr);
}
if (shoppingCart.selectedPayments() == 0 && profiledefs?.defaultPayMeth) {
    shoppingCart.addPayment(profiledefs.defaultPayMeth);
}

// create a list containing all the parties associated to the current cart, useful to change
// the ship to party id
cartParties = [shoppingCart.getShipToCustomerPartyId()];
if (!cartParties.contains(partyId)) {
    cartParties.add(partyId);
}
if (!cartParties.contains(shoppingCart.getOrderPartyId())) {
    cartParties.add(shoppingCart.getOrderPartyId());
}
if (!cartParties.contains(shoppingCart.getPlacingCustomerPartyId())) {
    cartParties.add(shoppingCart.getPlacingCustomerPartyId());
}
if (!cartParties.contains(shoppingCart.getBillToCustomerPartyId())) {
    cartParties.add(shoppingCart.getBillToCustomerPartyId());
}
if (!cartParties.contains(shoppingCart.getEndUserCustomerPartyId())) {
    cartParties.add(shoppingCart.getEndUserCustomerPartyId());
}
if (!cartParties.contains(shoppingCart.getSupplierAgentPartyId())) {
    cartParties.add(shoppingCart.getSupplierAgentPartyId());
}
salesReps = shoppingCart.getAdditionalPartyRoleMap().SALES_REP;
if (salesReps) {
    salesReps.each { salesRep ->
        if (!cartParties.contains(salesRep)) {
            cartParties.add(salesRep);
        }
    }
}
context.cartParties = cartParties;
