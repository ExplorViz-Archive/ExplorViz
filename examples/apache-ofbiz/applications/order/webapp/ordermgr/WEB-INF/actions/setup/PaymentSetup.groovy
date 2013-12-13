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
import org.ofbiz.base.util.*;

paymentSetup = delegator.findList("WebSitePaymentSettingView", null, null, ["webSiteId", "paymentMethodTypeId"], null, false);
context.paymentSetups = paymentSetup;

webSiteId = parameters.webSiteId;
paymentMethodTypeId = parameters.paymentMethodTypeId;

webSitePayment = null;
if (webSiteId && paymentMethodTypeId) {
    webSitePayment = delegator.findByPrimaryKey("WebSitePaymentSettingView", [webSiteId : webSiteId, paymentMethodTypeId : paymentMethodTypeId]);
}
context.webSitePayment = webSitePayment;

webSites = delegator.findList("WebSite", null, null, ["siteName"], null, false);
context.webSites = webSites;

paymentMethodTypes = delegator.findList("PaymentMethodType", null, null, ["description"], null, false);
context.paymentMethodTypes = paymentMethodTypes;

payInfo = UtilHttp.getParameterMap(request);
if (webSitePayment) {
    payInfo = webSitePayment;
}
context.payInfo = payInfo;
