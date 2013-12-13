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

import java.text.DateFormat;
import org.ofbiz.accounting.invoice.InvoiceWorker;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.entity.condition.EntityCondition;

invoiceDetailList = [];
invoiceIds.each { invoiceId ->
    invoicesMap = [:];
    invoice = delegator.findOne("Invoice", [invoiceId : invoiceId], false);
    invoicesMap.invoice = invoice;
    
    currency = parameters.currency;  // allow the display of the invoice in the original currency, the default is to display the invoice in the default currency
    BigDecimal conversionRate = new BigDecimal("1");
    ZERO = BigDecimal.ZERO;
    decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
    rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");
    
    if (invoice) {
        if (currency && !invoice.getString("currencyUomId").equals(currency)) {
            conversionRate = InvoiceWorker.getInvoiceCurrencyConversionRate(invoice);
            invoice.currencyUomId = currency;
            invoice.invoiceMessage = " converted from original with a rate of: " + conversionRate.setScale(8, rounding);
        }
    
        invoiceItems = invoice.getRelatedOrderBy("InvoiceItem", ["invoiceItemSeqId"]);
        invoiceItemsConv = [];
        invoiceItems.each { invoiceItem ->
          if (invoiceItem.amount) {
              invoiceItem.amount = invoiceItem.getBigDecimal("amount").multiply(conversionRate).setScale(decimals, rounding);
              invoiceItemsConv.add(invoiceItem);
          }
        }
    
        invoicesMap.invoiceItems = invoiceItemsConv;
    
        invoiceTotal = InvoiceWorker.getInvoiceTotal(invoice).multiply(conversionRate).setScale(decimals, rounding);
        invoiceNoTaxTotal = InvoiceWorker.getInvoiceNoTaxTotal(invoice).multiply(conversionRate).setScale(decimals, rounding);
        invoicesMap.invoiceTotal = invoiceTotal;
        invoicesMap.invoiceNoTaxTotal = invoiceNoTaxTotal;
    
        if ("PURCHASE_INVOICE".equals(invoice.invoiceTypeId)) {
            billingAddress = InvoiceWorker.getSendFromAddress(invoice);
        } else {
            billingAddress = InvoiceWorker.getBillToAddress(invoice);
        }
        if (billingAddress) {
            invoicesMap.billingAddress = billingAddress;
        }
        billingParty = InvoiceWorker.getBillToParty(invoice);
        invoicesMap.billingParty = billingParty;
        sendingParty = InvoiceWorker.getSendFromParty(invoice);
        invoicesMap.sendingParty = sendingParty;

        // This snippet was added for adding Tax ID in invoice header if needed 
        sendingTaxInfos = sendingParty.getRelated("PartyTaxAuthInfo");
        billingTaxInfos = billingParty.getRelated("PartyTaxAuthInfo");
        sendingPartyTaxId = null;
        billingPartyTaxId = null;

        if (billingAddress) {
            sendingTaxInfos.eachWithIndex { sendingTaxInfo, i ->
                if (sendingTaxInfo.taxAuthGeoId.equals(billingAddress.countryGeoId)) {
                     sendingPartyTaxId = sendingTaxInfos[i-1].partyTaxId;
                }
            }
            billingTaxInfos.eachWithIndex { billingTaxInfo, i ->
                if (billingTaxInfo.taxAuthGeoId.equals(billingAddress.countryGeoId)) {
                     billingPartyTaxId = billingTaxInfos[i-1].partyTaxId;
                }
            }
        }
        if (sendingPartyTaxId) {
            invoicesMap.sendingPartyTaxId = sendingPartyTaxId;
        }
        if (billingPartyTaxId) {
            invoicesMap.billingPartyTaxId = billingPartyTaxId;
        }
    
        terms = invoice.getRelated("InvoiceTerm");
        invoicesMap.terms = terms;
    
        paymentAppls = delegator.findList("PaymentApplication", EntityCondition.makeCondition([invoiceId : invoiceId]), null, null, null, false);
        invoicesMap.payments = paymentAppls;
    
        orderItemBillings = delegator.findList("OrderItemBilling", EntityCondition.makeCondition([invoiceId : invoiceId]), null, ['orderId'], null, false);
        orders = new LinkedHashSet();
        orderItemBillings.each { orderIb ->
            orders.add(orderIb.orderId);
        }
        invoicesMap.orders = orders;
    
        invoiceStatus = invoice.getRelatedOne("StatusItem");
        invoicesMap.invoiceStatus = invoiceStatus;
    
        edit = parameters.editInvoice;
        if ("true".equalsIgnoreCase(edit)) {
            invoiceItemTypes = delegator.findList("InvoiceItemType", null, null, null, null, false);
            invoicesMap.invoiceItemTypes = invoiceItemTypes;
            invoicesMap.editInvoice = true;
        }
    
        // format the date
        if (invoice.invoiceDate) {
            invoiceDate = DateFormat.getDateInstance(DateFormat.LONG).format(invoice.invoiceDate);
            invoicesMap.invoiceDate = invoiceDate;
        } else {
            invoicesMap.invoiceDate = "N/A";
        }
    }
    invoiceDetailList.add(invoicesMap);
}

context.invoiceDetailList = invoiceDetailList;
