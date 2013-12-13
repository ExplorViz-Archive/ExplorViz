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
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.common.email.NotificationServices;

orderId = request.getParameter("orderId") ?: parameters.get("orderId");
context.orderId = orderId;

partyId = request.getParameter("partyId");
sendTo = request.getParameter("sendTo");

context.partyId = partyId;
context.sendTo = sendTo;

donePage = request.getParameter("DONE_PAGE") ?: "orderview";
context.donePage = donePage;

// Provide the correct order confirmation ProductStoreEmailSetting, if one exists
orderHeader = delegator.findByPrimaryKey("OrderHeader", [orderId : orderId]);
if (orderHeader.productStoreId) {
    productStoreEmailSetting = delegator.findByPrimaryKeyCache("ProductStoreEmailSetting", [productStoreId : orderHeader.productStoreId, emailType : emailType]);
    if (productStoreEmailSetting) {
        context.productStoreEmailSetting = productStoreEmailSetting;
    }
}

// set the baseUrl parameter, required by some email bodies
NotificationServices.setBaseUrl(delegator, context.webSiteId, context);
