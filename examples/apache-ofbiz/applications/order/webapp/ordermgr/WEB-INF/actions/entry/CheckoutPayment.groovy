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

cart = session.getAttribute("shoppingCart");
currencyUomId = cart.getCurrency();
userLogin = session.getAttribute("userLogin");
partyId = cart.getPartyId();
party = delegator.findByPrimaryKeyCache("Party", [partyId : partyId]);
productStoreId = ProductStoreWorker.getProductStoreId(request);

checkOutPaymentId = "";
if (cart) {
    if (cart.getPaymentMethodIds()) {
        checkOutPaymentId = cart.getPaymentMethodIds().get(0);
    } else if (cart.getPaymentMethodTypeIds()) {
        checkOutPaymentId = cart.getPaymentMethodTypeIds().get(0);
    }
}

finAccounts = delegator.findByAnd("FinAccountAndRole", [partyId : partyId, roleTypeId : "OWNER"]);
finAccounts = EntityUtil.filterByDate(finAccounts, UtilDateTime.nowTimestamp(), "roleFromDate", "roleThruDate", true);
finAccounts = EntityUtil.filterByDate(finAccounts);
context.finAccounts = finAccounts;

context.shoppingCart = cart;
context.userLogin = userLogin;
context.productStoreId = productStoreId;
context.checkOutPaymentId = checkOutPaymentId;
context.paymentMethodList = EntityUtil.filterByDate(party.getRelated("PaymentMethod", null, ["paymentMethodTypeId"]), true);

billingAccountList = BillingAccountWorker.makePartyBillingAccountList(userLogin, currencyUomId, partyId, delegator, dispatcher);
if (billingAccountList) {
    context.selectedBillingAccountId = cart.getBillingAccountId();
    context.billingAccountList = billingAccountList;
}

checkIdealPayment = false;
productStore = ProductStoreWorker.getProductStore(request);
productStorePaymentSettingList = productStore.getRelatedCache("ProductStorePaymentSetting");
productStorePaymentSettingIter = productStorePaymentSettingList.iterator();
while (productStorePaymentSettingIter.hasNext()) {
    productStorePaymentSetting = productStorePaymentSettingIter.next();
    if (productStorePaymentSetting.get("paymentMethodTypeId") == "EXT_IDEAL") {
        checkIdealPayment = true;
    }
    
}

if (checkIdealPayment) {
    issuerList = org.ofbiz.accounting.thirdparty.ideal.IdealEvents.getIssuerList();
    if (issuerList) {
        context.issuerList = issuerList;
    }
}