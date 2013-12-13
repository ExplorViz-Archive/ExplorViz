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

import org.ofbiz.entity.*
import org.ofbiz.entity.condition.*
import org.ofbiz.entity.transaction.*

action = request.getParameter("action");

inventoryItemTotals = [];
qohGrandTotal = 0.0;
atpGrandTotal = 0.0;
costPriceGrandTotal = 0.0;
retailPriceGrandTotal = 0.0;
totalCostPriceGrandTotal = 0.0;
totalRetailPriceGrandTotal = 0.0;
boolean beganTransaction = false;
if (action) {
    conditions = [EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INV_DELIVERED")];
    conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, null));
    conditionList = EntityCondition.makeCondition(conditions, EntityOperator.OR);
    try {
        // create resultMap to stop issue with the first puts in the while loop
        resultMap = [:];
        beganTransaction = TransactionUtil.begin();
        invItemListItr = delegator.find("InventoryItem", conditionList, null, null, ['productId'], null);
        while ((inventoryItem = invItemListItr.next()) != null) {
            productId = inventoryItem.productId;
            product = delegator.findOne("Product", [productId : productId], false);
            productFacility = delegator.findOne("ProductFacility", [productId : productId, facilityId : facilityId], false);
            if (productFacility) {
                quantityOnHandTotal = inventoryItem.getDouble("quantityOnHandTotal");
                availableToPromiseTotal = inventoryItem.getDouble("availableToPromiseTotal");
                costPrice = inventoryItem.getDouble("unitCost");
                retailPrice = 0.0;
                productPrices = product.getRelated("ProductPrice");
                if (productPrices) {
                    productPrices.each { productPrice ->
                        if (("DEFAULT_PRICE").equals(productPrice.productPriceTypeId)) {
                            retailPrice = productPrice.getDouble("price");
                        }
                    }
                }
                if (costPrice && quantityOnHandTotal) {
                    totalCostPrice = costPrice * quantityOnHandTotal;
                    resultMap.totalCostPrice = totalCostPrice;
                    totalCostPriceGrandTotal += totalCostPrice;
                }
                if (retailPrice && quantityOnHandTotal) {
                    totalRetailPrice = retailPrice * quantityOnHandTotal;
                    resultMap.totalRetailPrice = totalRetailPrice;
                    totalRetailPriceGrandTotal += totalRetailPrice;
                }
                if (quantityOnHandTotal) {
                    qohGrandTotal += quantityOnHandTotal;
                }
                if (availableToPromiseTotal) {
                    atpGrandTotal += availableToPromiseTotal;
                }
                if (costPrice) {
                    costPriceGrandTotal += costPrice;
                }
                if (retailPrice) {
                    retailPriceGrandTotal += retailPrice;
                }

                resultMap = [productId : product.productId, quantityOnHand : quantityOnHandTotal, availableToPromise : availableToPromiseTotal,
                             costPrice : costPrice, retailPrice : retailPrice];
                inventoryItemTotals.add(resultMap);
            }
        }
        invItemListItr.close();
    } catch (GenericEntityException e) {
        errMsg = "Failure in operation, rolling back transaction";
        Debug.logError(e, errMsg, "findInventoryItemsByLabels");
        try {
            // only rollback the transaction if we started one...
            TransactionUtil.rollback(beganTransaction, errMsg, e);
        } catch (GenericEntityException e2) {
            Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), "findInventoryItemsByLabels");
        }
        // after rolling back, rethrow the exception
        throw e;
    } finally {
        // only commit the transaction if we started one... this will throw an exception if it fails
        TransactionUtil.commit(beganTransaction);
    }

}

inventoryItemGrandTotals = [];
inventoryItemGrandTotals.add([qohGrandTotal : qohGrandTotal, atpGrandTotal : atpGrandTotal,
                              totalCostPriceGrandTotal : totalCostPriceGrandTotal, totalRetailPriceGrandTotal : totalRetailPriceGrandTotal]);

context.inventoryItemTotals = inventoryItemTotals;
context.inventoryItemGrandTotals = inventoryItemGrandTotals;
