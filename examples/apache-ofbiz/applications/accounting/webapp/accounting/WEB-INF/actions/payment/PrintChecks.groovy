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

import java.util.ArrayList;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

// rounding mode
decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");
context.decimals = decimals;
context.rounding = rounding;

// list of payments
payments = [];

// first ensure ability to print
security = request.getAttribute("security");
context.put("security", security);
if (!security.hasEntityPermission("ACCOUNTING", "_PRINT_CHECKS", session)) {
    context.payments = payments; // if no permission, just pass an empty list for now
    return;
}

// in the case of a single payment, the paymentId will be supplied
paymentId = context.paymentId;
if (paymentId) {
    payment = delegator.findByPrimaryKey("Payment", [paymentId : paymentId]);
    if (payment) payments.add(payment);
    context.payments = payments;
    return;
}

// in the case of a multi form, parse the multi data and get all of the selected payments
selected = UtilHttp.parseMultiFormData(parameters);
selected.each { row ->
    payment = delegator.findByPrimaryKey("Payment", [paymentId : row.paymentId]);
    if (payment) {
        payments.add(payment);
    }
}
paymentGroupMembers = EntityUtil.filterByDate(delegator.findList("PaymentGroupMember", EntityCondition.makeCondition("paymentGroupId", EntityOperator.EQUALS, parameters.paymentGroupId), null, null, null, false));
//in the case of a multiple payments, paymentId List is supplied.
paymentGroupMembers.each { paymentGropupMember->
    payments.add(paymentGropupMember.getRelatedOne("Payment"));
}
context.payments = payments;
