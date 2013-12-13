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

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.product.product.ProductContentWrapper;

bestSellingProducts = [];
exprList = [];
exprList.add(EntityCondition.makeCondition("orderDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(filterDate)));
exprList.add(EntityCondition.makeCondition("orderDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(filterDate)));
exprList.add(EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER"));

orderHeaderList = delegator.findList("OrderHeader", EntityCondition.makeCondition(exprList, EntityOperator.AND), null, null, null, false);

orderHeaderList.each { orderHeader ->
    exprList = [];
    exprList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderHeader.orderId));
    exprList.add(EntityCondition.makeCondition("orderItemTypeId", EntityOperator.EQUALS, "PRODUCT_ORDER_ITEM"));
    exprList.add(EntityCondition.makeCondition("isPromo", EntityOperator.EQUALS, "N"));
    orderItemList = delegator.findList("OrderItem", EntityCondition.makeCondition(exprList, EntityOperator.AND), null, null, null, false);
    
    orderItemList.each { orderItem ->
        orderItemDetail = [:];
        qtyOrdered = BigDecimal.ZERO;
        qtyOrdered += orderItem.quantity;
        if (orderItem.cancelQuantity) {
            qtyOrdered -= orderItem.cancelQuantity;
        }
        amount = BigDecimal.ZERO;;
        amount = qtyOrdered * orderItem.unitPrice;
        inListFlag = false
        
        bestSellingProducts.each { bestSellingProduct ->
            if ((bestSellingProduct.productId).equals(orderItem.productId)) {
                inListFlag = true;
                bestSellingProduct.amount += amount;
                bestSellingProduct.qtyOrdered += qtyOrdered;
            }
        }
        
        if (inListFlag == false) {
            orderItemDetail.productId = orderItem.productId;
            product = delegator.findOne("Product", [productId : orderItem.productId], false);
            contentWrapper = new ProductContentWrapper(product, request);
            orderItemDetail.productName = contentWrapper.get("PRODUCT_NAME");
            orderItemDetail.amount = amount;
            orderItemDetail.qtyOrdered = qtyOrdered;
            bestSellingProducts.add(orderItemDetail);
        }
    }
}

// Sorting List
topSellingProducts = [];
itr = 1;
while (itr <= 5) {
    orderItemDetail = [:];
    bestSellingProducts.each { bestSellingProduct ->
        if (!(orderItemDetail.isEmpty())) {
            if (bestSellingProduct.qtyOrdered > orderItemDetail.qtyOrdered) {
                orderItemDetail = bestSellingProduct;
            }
            if (bestSellingProduct.qtyOrdered == orderItemDetail.qtyOrdered && bestSellingProduct.amount > orderItemDetail.amount) {
                orderItemDetail = bestSellingProduct;
            }
        } else {
            orderItemDetail = bestSellingProduct;
        }
    }
    if (!orderItemDetail.isEmpty()) {
        if (orderItemDetail.amount) {
            orderItemDetail.amount = orderItemDetail.amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        topSellingProducts.add(orderItemDetail);
        bestSellingProducts.remove(orderItemDetail);
    }
    itr++;
}

context.bestSellingProducts = topSellingProducts;

context.now = UtilDateTime.toDateString(UtilDateTime.nowDate());
