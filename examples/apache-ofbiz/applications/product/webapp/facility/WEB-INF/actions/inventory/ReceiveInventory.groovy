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
import org.ofbiz.entity.util.*
import org.ofbiz.entity.condition.*
import org.ofbiz.service.ServiceUtil

facilityId = request.getParameter("facilityId");
purchaseOrderId = request.getParameter("purchaseOrderId");
productId = request.getParameter("productId");
shipmentId = request.getParameter("shipmentId");

partialReceive = parameters.partialReceive;
if (partialReceive) {
    context.partialReceive = partialReceive;
}

facility = null;
if (facilityId) {
    facility = delegator.findOne("Facility", [facilityId : facilityId], false);
}

ownerAcctgPref = null;
if (facility) {
    owner = facility.getRelatedOne("OwnerParty");
    if (owner) {
        result = dispatcher.runSync("getPartyAccountingPreferences", [organizationPartyId : owner.partyId, userLogin : request.getAttribute("userLogin")]);
        if (!ServiceUtil.isError(result) && result.partyAccountingPreference) {
            ownerAcctgPref = result.partyAccountingPreference;
        }
    }
}

purchaseOrder = null;
if (purchaseOrderId) {
    purchaseOrder = delegator.findOne("OrderHeader", [orderId : purchaseOrderId], false);
    if (purchaseOrder && !"PURCHASE_ORDER".equals(purchaseOrder.orderTypeId)) {
        purchaseOrder = null;
    }
}

product = null;
if (productId) {
    product = delegator.findOne("Product", [productId : productId], false);
    context.supplierPartyIds = EntityUtil.getFieldListFromEntityList(EntityUtil.filterByDate(delegator.findList("SupplierProduct", EntityCondition.makeCondition([productId : productId]), null, ["partyId"], null, false), nowTimestamp, "availableFromDate", "availableThruDate", true), "partyId", true);
}

shipments = null;
if (purchaseOrder && !shipmentId) {
    orderShipments = delegator.findList("OrderShipment", EntityCondition.makeCondition([orderId : purchaseOrderId]), null, null, null, false);
    if (orderShipments) {
        shipments = [] as TreeSet;
        orderShipments.each { orderShipment ->
            shipment = orderShipment.getRelatedOne("Shipment");
            if (!"PURCH_SHIP_RECEIVED".equals(shipment.statusId) &&
                !"SHIPMENT_CANCELLED".equals(shipment.statusId) &&
                (!shipment.destinationFacilityId || facilityId.equals(shipment.destinationFacilityId))) {
                shipments.add(shipment);
            }
        }
    }
    // This is here for backward compatibility: ItemIssuances are no more created for purchase shipments.
    issuances = delegator.findList("ItemIssuance", EntityCondition.makeCondition([orderId : purchaseOrderId]), null, null, null, false);
    if (issuances) {
        shipments = [] as TreeSet;
        issuances.each { issuance ->
            shipment = issuance.getRelatedOne("Shipment");
            if (!"PURCH_SHIP_RECEIVED".equals(shipment.statusId) &&
                !"SHIPMENT_CANCELLED".equals(shipment.statusId) &&
                (!shipment.destinationFacilityId || facilityId.equals(shipment.destinationFacilityId))) {
                shipments.add(shipment);
            }
        }
    }
}

shipment = null;
if (shipmentId && !shipmentId.equals("_NA_")) {
    shipment = delegator.findOne("Shipment", [shipmentId : shipmentId], false);
}

shippedQuantities = [:];
purchaseOrderItems = null;
if (purchaseOrder) {
    if (product) {
        purchaseOrderItems = purchaseOrder.getRelated("OrderItem", [productId : productId], null);
    } else if (shipment) {
        orderItems = purchaseOrder.getRelated("OrderItem");
        exprs = [] as ArrayList;
        orderShipments = shipment.getRelated("OrderShipment", [orderId : purchaseOrderId], null);
        if (orderShipments) {
            orderShipments.each { orderShipment ->
                exprs.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, orderShipment.orderItemSeqId));
                double orderShipmentQty = orderShipment.getDouble("quantity").doubleValue();
                if (shippedQuantities.containsKey(orderShipment.orderItemSeqId)) {
                    orderShipmentQty += ((Double)shippedQuantities.get(orderShipment.orderItemSeqId)).doubleValue();
                }
                shippedQuantities.put(orderShipment.orderItemSeqId, orderShipmentQty);
            }
        } else {
            // this is here for backward compatibility only: ItemIssuances are no more created for purchase shipments.
            issuances = shipment.getRelated("ItemIssuance", [orderId : purchaseOrderId], null);
            issuances.each { issuance ->
                exprs.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, issuance.orderItemSeqId));
                double issuanceQty = issuance.getDouble("quantity").doubleValue();
                if (shippedQuantities.containsKey(issuance.orderItemSeqId)) {
                    issuanceQty += ((Double)shippedQuantities.get(issuance.orderItemSeqId)).doubleValue();
                }
                shippedQuantities.put(issuance.orderItemSeqId, issuanceQty);
            }
        }
        purchaseOrderItems = EntityUtil.filterByOr(orderItems, exprs);
    } else {
        purchaseOrderItems = purchaseOrder.getRelated("OrderItem");
    }
    purchaseOrderItems = EntityUtil.filterByAnd(purchaseOrderItems, [EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_CANCELLED")]);
}
// convert the unit prices to that of the facility owner's currency
orderCurrencyUnitPriceMap = [:];
if (purchaseOrder && facility) {
    if (ownerAcctgPref) {
        ownerCurrencyUomId = ownerAcctgPref.baseCurrencyUomId;
        orderCurrencyUomId = purchaseOrder.currencyUom;
        if (!orderCurrencyUomId.equals(ownerCurrencyUomId)) {
            purchaseOrderItems.each { item ->
            orderCurrencyUnitPriceMap.(item.orderItemSeqId) = item.unitPrice;
                serviceResults = dispatcher.runSync("convertUom",
                        [uomId : orderCurrencyUomId, uomIdTo : ownerCurrencyUomId, originalValue : item.unitPrice]);
                if (ServiceUtil.isError(serviceResults)) {
                    request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(serviceResults));
                    return;
                } else {
                    convertedValue = serviceResults.convertedValue;
                    if (convertedValue) {
                        item.unitPrice = convertedValue;
                    }
                }
            }
        }

        // put the pref currency in the map for display and form use
        context.currencyUomId = ownerCurrencyUomId;
        context.orderCurrencyUomId = orderCurrencyUomId;
    } else {
        request.setAttribute("_ERROR_MESSAGE_", "Either no owner party was set for this facility, or no accounting preferences were set for this owner party.");
    }
}
context.orderCurrencyUnitPriceMap = orderCurrencyUnitPriceMap;

receivedQuantities = [:];
salesOrderItems = [:];
if (purchaseOrderItems) {
    context.firstOrderItem = EntityUtil.getFirst(purchaseOrderItems);
    context.purchaseOrderItemsSize = purchaseOrderItems.size();
    purchaseOrderItems.each { thisItem ->
        totalReceived = 0.0;
        receipts = thisItem.getRelated("ShipmentReceipt");
        if (receipts) {
            receipts.each { rec ->
                if (!shipment || (rec.shipmentId && rec.shipmentId.equals(shipment.shipmentId))) {
                    accepted = rec.getDouble("quantityAccepted");
                    rejected = rec.getDouble("quantityRejected");
                    if (accepted) {
                        totalReceived += accepted.doubleValue();
                    }
                    if (rejected) {
                        totalReceived += rejected.doubleValue();
                    }
                }
            }
        }
        receivedQuantities.put(thisItem.orderItemSeqId, new Double(totalReceived));
        //----------------------
        salesOrderItemAssocs = delegator.findList("OrderItemAssoc", EntityCondition.makeCondition([orderItemAssocTypeId : 'PURCHASE_ORDER',
                                                                     toOrderId : thisItem.orderId,
                                                                     toOrderItemSeqId : thisItem.orderItemSeqId]),
                                                                     null, null, null, false);
        if (salesOrderItemAssocs) {
            salesOrderItem = EntityUtil.getFirst(salesOrderItemAssocs);
            salesOrderItems.put(thisItem.orderItemSeqId, salesOrderItem);
        }
    }
}

receivedItems = null;
if (purchaseOrder) {
    receivedItems = delegator.findList("ShipmentReceiptAndItem", EntityCondition.makeCondition([orderId : purchaseOrderId, facilityId : facilityId]), null, null, null, false);
    context.receivedItems = receivedItems;
}

invalidProductId = null;
if (productId && !product) {
    invalidProductId = "No product found with product ID: [" + productId + "]";
    context.invalidProductId = invalidProductId;
}

// reject reasons
rejectReasons = delegator.findList("RejectionReason", null, null, null, null, false);

// inv item types
inventoryItemTypes = delegator.findList("InventoryItemType", null, null, null, null, false);

// facilities
facilities = delegator.findList("Facility", null, null, null, null, false);

// default per unit cost for both shipment or individual product
standardCosts = [:];
if (ownerAcctgPref) {

    // get the unit cost of the products in a shipment
    if (purchaseOrderItems) {
        purchaseOrderItems.each { orderItem ->
            productId = orderItem.productId;
            if (productId) {
                result = dispatcher.runSync("getProductCost", [productId : productId, currencyUomId : ownerAcctgPref.baseCurrencyUomId,
                                                               costComponentTypePrefix : 'EST_STD', userLogin : request.getAttribute("userLogin")]);
                if (!ServiceUtil.isError(result)) {
                    standardCosts.put(productId, result.productCost);
                }
            }
        }
    }

    // get the unit cost of a single product
    if (productId) {
        result = dispatcher.runSync("getProductCost", [productId : productId, currencyUomId : ownerAcctgPref.baseCurrencyUomId,
                                                       costComponentTypePrefix : 'EST_STD', userLogin : request.getAttribute("userLogin")]);
        if (!ServiceUtil.isError(result)) {
            standardCosts.put(productId, result.productCost);
        }
    }
}

context.facilityId = facilityId;
context.facility = facility;
context.purchaseOrder = purchaseOrder;
context.product = product;
context.shipments = shipments;
context.shipment = shipment;
context.shippedQuantities = shippedQuantities;
context.purchaseOrderItems = purchaseOrderItems;
context.receivedQuantities = receivedQuantities;
context.salesOrderItems = salesOrderItems;
context.rejectReasons = rejectReasons;
context.inventoryItemTypes = inventoryItemTypes;
context.facilities = facilities;
context.standardCosts = standardCosts;
