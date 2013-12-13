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

import java.util.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.shipping.*;
import org.ofbiz.order.shoppinglist.*;
import org.ofbiz.party.contact.*;
import org.ofbiz.product.catalog.*;
import org.ofbiz.product.store.*;
import org.ofbiz.service.calendar.*;

party = userLogin.getRelatedOne("Party");

cart = ShoppingCartEvents.getCartObject(request);
currencyUomId = cart.getCurrency();

productStoreId = ProductStoreWorker.getProductStoreId(request);
prodCatalogId = CatalogWorker.getCurrentCatalogId(request);
webSiteId = CatalogWorker.getWebSiteId(request);

context.productStoreId = productStoreId;
context.currencyUomId = currencyUomId;

// get the top level shopping lists for the logged in user
exprList = [EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, userLogin.partyId),
        EntityCondition.makeCondition("listName", EntityOperator.NOT_EQUAL, "auto-save")];
condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
allShoppingLists = delegator.findList("ShoppingList", condition, null, ["listName"], null, false);
shoppingLists = EntityUtil.filterByAnd(allShoppingLists, [parentShoppingListId : null]);
context.allShoppingLists = allShoppingLists;
context.shoppingLists = shoppingLists;

// get all shoppingListTypes
shoppingListTypes = delegator.findList("ShoppingListType", null, null, ["description"], null, true);
context.shoppingListTypes = shoppingListTypes;

// get the shoppingListId for this reqest
parameterMap = UtilHttp.getParameterMap(request);
shoppingListId = parameterMap.shoppingListId ?: request.getAttribute("shoppingListId") ?: session.getAttribute("currentShoppingListId");
context.shoppingListId = shoppingListId;

// no passed shopping list id default to first list
if (!shoppingListId) {
    firstList = EntityUtil.getFirst(shoppingLists);
    if (firstList) {
        shoppingListId = firstList.shoppingListId;
    }
}
session.setAttribute("currentShoppingListId", shoppingListId);

// if we passed a shoppingListId get the shopping list info
if (shoppingListId) {
    shoppingList = delegator.findByPrimaryKey("ShoppingList", [shoppingListId : shoppingListId]);
    context.shoppingList = shoppingList;

    if (shoppingList) {
        shoppingListItemTotal = 0.0;
        shoppingListChildTotal = 0.0;

        shoppingListItems = shoppingList.getRelatedCache("ShoppingListItem");
        if (shoppingListItems) {
            shoppingListItemDatas = new ArrayList(shoppingListItems.size());
            shoppingListItems.each { shoppingListItem ->
                shoppingListItemData = [:];

                product = shoppingListItem.getRelatedOneCache("Product");

                calcPriceInMap = [product : product, quantity : shoppingListItem.quantity, currencyUomId : currencyUomId, userLogin : userLogin];
                calcPriceInMap.webSiteId = webSiteId;
                calcPriceInMap.prodCatalogId = prodCatalogId;
                calcPriceInMap.productStoreId = productStoreId;
                calcPriceOutMap = dispatcher.runSync("calculateProductPrice", calcPriceInMap);
                price = calcPriceOutMap.price;
                totalPrice = price * shoppingListItem.quantity;
                // similar code at ShoppingCartItem.java getRentalAdjustment
                if ("ASSET_USAGE".equals(product.productTypeId) || "ASSET_USAGE_OUT_IN".equals(product.productTypeId)) {
                    persons = shoppingListItem.reservPersons ?: 0;
                    reservNthPPPerc = product.reservNthPPPerc ?: 0;
                    reserv2ndPPPerc = product.reserv2ndPPPerc ?: 0;
                    rentalValue = 0;
                    if (persons) {
                        if (persons > 2) {
                            persons -= 2;
                            if (reservNthPPPerc) {
                                rentalValue = persons * reservNthPPPerc;
                            } else if (reserv2ndPPPerc) {
                                rentalValue = persons * reserv2ndPPPerc;
                            }
                            persons = 2;
                        }
                        if (persons == 2) {
                            if (reserv2ndPPPerc) {
                                rentalValue += reserv2ndPPPerc;
                            } else if (reservNthPPPerc) {
                                rentalValue = persons * reservNthPPPerc;
                            }
                        }
                    }
                    rentalValue += 100;    // add final 100 percent for first person
                    reservLength = shoppingListItem.reservLength ?: 0;
                    totalPrice *= (rentalValue/100 * reservLength);
                }
                shoppingListItemTotal += totalPrice;

                productVariantAssocs = null;
                if ("Y".equals(product.isVirtual)) {
                    productVariantAssocs = product.getRelatedCache("MainProductAssoc", [productAssocTypeId : "PRODUCT_VARIANT"], ["sequenceNum"]);
                    productVariantAssocs = EntityUtil.filterByDate(productVariantAssocs);
                }
                shoppingListItemData.shoppingListItem = shoppingListItem;
                shoppingListItemData.product = product;
                shoppingListItemData.unitPrice = price;
                shoppingListItemData.totalPrice = totalPrice;
                shoppingListItemData.productVariantAssocs = productVariantAssocs;
                shoppingListItemDatas.add(shoppingListItemData);
            }
            context.shoppingListItemDatas = shoppingListItemDatas;
            // pagination for the shopping list
            viewIndex = Integer.valueOf(parameters.VIEW_INDEX  ?: 1);
            viewSize = Integer.valueOf(parameters.VIEW_SIZE ?: 20);
            listSize = 0;
            if (shoppingListItemDatas)
                listSize = shoppingListItemDatas.size();
            
            lowIndex = (((viewIndex - 1) * viewSize) + 1);
            highIndex = viewIndex * viewSize;
            if (highIndex > listSize) {
                highIndex = listSize;
            }
            context.viewIndex = viewIndex;
            context.viewSize = viewSize;
            context.listSize = listSize;
            context.lowIndex = lowIndex;
            context.highIndex = highIndex;
        }

        shoppingListType = shoppingList.getRelatedOne("ShoppingListType");
        context.shoppingListType = shoppingListType;

        // get the child shopping lists of the current list for the logged in user
        childShoppingLists = delegator.findByAndCache("ShoppingList", [partyId : userLogin.partyId, parentShoppingListId : shoppingListId], ["listName"]);
        // now get prices for each child shopping list...
        if (childShoppingLists) {
            childShoppingListDatas = new ArrayList(childShoppingLists.size());
            childShoppingLists.each { childShoppingList ->
                childShoppingListData = [:];

                calcListPriceInMap = [shoppingListId : childShoppingList.shoppingListId, prodCatalogId : prodCatalogId, webSiteId : webSiteId, userLogin : userLogin, currencyUomId : currencyUomId];
                childShoppingListPriceMap = dispatcher.runSync("calculateShoppingListDeepTotalPrice", calcListPriceInMap);
                totalPrice = childShoppingListPriceMap.totalPrice;
                shoppingListChildTotal += totalPrice;

                childShoppingListData.childShoppingList = childShoppingList;
                childShoppingListData.totalPrice = totalPrice;
                childShoppingListDatas.add(childShoppingListData);
            }
            context.childShoppingListDatas = childShoppingListDatas;
        }
        context.shoppingListTotalPrice = shoppingListItemTotal + shoppingListChildTotal;
        context.shoppingListItemTotal = shoppingListItemTotal;
        context.shoppingListChildTotal = shoppingListChildTotal;

        // get the parent shopping list if there is one
        parentShoppingList = shoppingList.getRelatedOne("ParentShoppingList");
        context.parentShoppingList = parentShoppingList;

        context.canView = userLogin.partyId.equals(shoppingList.partyId);

        // auto-reorder info
        if ("SLT_AUTO_REODR".equals(shoppingListType?.shoppingListTypeId)) {
            recurrenceVo = shoppingList.getRelatedOne("RecurrenceInfo");
            context.recurrenceInfo = recurrenceVo;

            if (userLogin.partyId.equals(shoppingList.partyId)) {
                listCart = ShoppingListServices.makeShoppingListCart(dispatcher, shoppingListId, locale);

                // get customer's shipping & payment info
                context.chosenShippingMethod = shoppingList.shipmentMethodTypeId + "@" + shoppingList.carrierPartyId;
                context.shippingContactMechList = ContactHelper.getContactMech(party, "SHIPPING_LOCATION", "POSTAL_ADDRESS", false);
                context.paymentMethodList = EntityUtil.filterByDate(party.getRelated("PaymentMethod", null, ["paymentMethodTypeId"]));

                shipAddress = delegator.findByPrimaryKey("PostalAddress", ["contactMechId" : shoppingList.contactMechId]);
                Debug.log("SL - address : " + shipAddress);
                if (shipAddress) {
                    listCart = ShoppingListServices.makeShoppingListCart(dispatcher, shoppingListId, locale);
                    if (listCart) {
                        shippingEstWpr = new ShippingEstimateWrapper(dispatcher, listCart, 0);
                        carrierShipMeths = shippingEstWpr.getShippingMethods();
                        context.listCart = listCart;
                        context.shippingEstWpr = shippingEstWpr;
                        context.carrierShipMethods = carrierShipMeths;
                    }
                }

                if (recurrenceVo) {
                    recInfo = new RecurrenceInfo(recurrenceVo);
                    context.recInfo = recInfo;
                    lastSlOrderDate = shoppingList.lastOrderedDate;
                    context.lastSlOrderDate = lastSlOrderDate;
                    if (!lastSlOrderDate) {
                        lastSlOrderDate = recurrenceVo.startDateTime;
                    }
                    context.lastSlOrderTime = lastSlOrderDate.getTime();
                }
            }
        }
    }
}
