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
import org.ofbiz.common.*;
import org.ofbiz.webapp.control.*;
import org.ofbiz.accounting.invoice.*;
import org.ofbiz.accounting.payment.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import javolution.util.FastMap;

Boolean actualCurrency = new Boolean(context.actualCurrency);
if (actualCurrency == null) {
    actualCurrency = true;
}
actualCurrencyUomId = context.actualCurrencyUomId;
if (!actualCurrencyUomId) {
    actualCurrencyUomId = context.defaultOrganizationPartyCurrencyUomId;
}
findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
//get total/unapplied/applied invoices separated by sales/purch amount:
totalInvSaApplied         = BigDecimal.ZERO;
totalInvSaNotApplied     = BigDecimal.ZERO;
totalInvPuApplied         = BigDecimal.ZERO;
totalInvPuNotApplied     = BigDecimal.ZERO;

invExprs =
    EntityCondition.makeCondition([
        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_IN_PROCESS"),
        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_WRITEOFF"),
        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"),
        EntityCondition.makeCondition([
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                ],EntityOperator.AND),
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                ],EntityOperator.AND)
            ],EntityOperator.OR)
        ],EntityOperator.AND);

invIterator = delegator.find("InvoiceAndType", invExprs, null, null, null, findOpts);

while (invoice = invIterator.next()) {
    if ("PURCHASE_INVOICE".equals(invoice.parentTypeId)) {
        totalInvPuApplied += InvoiceWorker.getInvoiceApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
        totalInvPuNotApplied += InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    else if ("SALES_INVOICE".equals(invoice.parentTypeId)) {
        totalInvSaApplied += InvoiceWorker.getInvoiceApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
        totalInvSaNotApplied += InvoiceWorker.getInvoiceNotApplied(invoice, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    else {
        Debug.logError("InvoiceType: " + invoice.invoiceTypeId + " without a valid parentTypeId: " + invoice.parentTypeId + " !!!! Should be either PURCHASE_INVOICE or SALES_INVOICE", "");
    }
}

invIterator.close();

//get total/unapplied/applied payment in/out total amount:
totalPayInApplied         = BigDecimal.ZERO;
totalPayInNotApplied     = BigDecimal.ZERO;
totalPayOutApplied         = BigDecimal.ZERO;
totalPayOutNotApplied     = BigDecimal.ZERO;

payExprs =
    EntityCondition.makeCondition([
        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_NOTPAID"),
        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_CANCELLED"),
        EntityCondition.makeCondition([
               EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, parameters.partyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, context.defaultOrganizationPartyId)
                ], EntityOperator.AND),
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, context.defaultOrganizationPartyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                ], EntityOperator.AND)
            ], EntityOperator.OR)
        ], EntityOperator.AND);

payIterator = delegator.find("PaymentAndType", payExprs, null, null, null, findOpts);

while (payment = payIterator.next()) {
    if ("DISBURSEMENT".equals(payment.parentTypeId) || "TAX_PAYMENT".equals(payment.parentTypeId)) {
        totalPayOutApplied += PaymentWorker.getPaymentApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
        totalPayOutNotApplied += PaymentWorker.getPaymentNotApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    else if ("RECEIPT".equals(payment.parentTypeId)) {
        totalPayInApplied += PaymentWorker.getPaymentApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
        totalPayInNotApplied += PaymentWorker.getPaymentNotApplied(payment, actualCurrency).setScale(2,BigDecimal.ROUND_HALF_UP);
    }
    else {
        Debug.logError("PaymentTypeId: " + payment.paymentTypeId + " without a valid parentTypeId: " + payment.parentTypeId + " !!!! Should be either DISBURSEMENT, TAX_PAYMENT or RECEIPT", "");
    }
}
payIterator.close();
context.finanSummary = FastMap.newInstance();
context.finanSummary.totalSalesInvoice         = totalInvSaApplied.add(totalInvSaNotApplied);
context.finanSummary.totalPurchaseInvoice     = totalInvPuApplied.add(totalInvPuNotApplied);
context.finanSummary.totalPaymentsIn         = totalPayInApplied.add(totalPayInNotApplied);
context.finanSummary.totalPaymentsOut         = totalPayOutApplied.add(totalPayOutNotApplied);
context.finanSummary.totalInvoiceNotApplied = totalInvSaNotApplied.subtract(totalInvPuNotApplied);
context.finanSummary.totalPaymentNotApplied = totalPayInNotApplied.subtract(totalPayOutNotApplied);
transferAmount = totalInvSaApplied.add(totalInvSaNotApplied).subtract(totalInvPuApplied.add(totalInvPuNotApplied)).subtract(totalPayInApplied.add(totalPayInNotApplied).add(totalPayOutApplied.add(totalPayOutNotApplied)));
if (transferAmount.signum() == -1) { // negative?
    context.finanSummary.totalToBeReceived = transferAmount.negate();
} else {
    context.finanSummary.totalToBePaid = transferAmount;
}

