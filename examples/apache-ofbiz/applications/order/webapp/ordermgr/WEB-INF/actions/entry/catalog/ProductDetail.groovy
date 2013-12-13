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

/*
 * This script is also referenced by the ecommerce's screens and
 * should not contain order component's specific code.
 */

import java.text.NumberFormat;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.service.*;
import org.ofbiz.webapp.taglib.*;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductSearch;
import org.ofbiz.product.product.ProductSearchSession;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.product.catalog.*;
import org.ofbiz.product.store.*;
import org.ofbiz.webapp.stats.VisitHandler;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCart;

String buildNext(Map map, List order, String current, String prefix, Map featureTypes) {
    def ct = 0;
    def buf = new StringBuffer();
    buf.append("function listFT" + current + prefix + "() { ");
    buf.append("document.forms[\"addform\"].elements[\"FT" + current + "\"].options.length = 1;");
    buf.append("document.forms[\"addform\"].elements[\"FT" + current + "\"].options[0] = new Option(\"" + featureTypes[current] + "\",\"\",true,true);");
    map.each { key, value ->
        def optValue = null;

        if (order.indexOf(current) == (order.size()-1)) {
            optValue = value.iterator().next();
        } else {
            optValue = prefix + "_" + ct;
        }

        buf.append("document.forms[\"addform\"].elements[\"FT" + current + "\"].options[" + (ct + 1) + "] = new Option(\"" + key + "\",\"" + optValue + "\");");
        ct++;
    }
    buf.append(" }");
    if (order.indexOf(current) < (order.size()-1)) {
        ct = 0;
        map.each { key, value ->
            def nextOrder = order.get(order.indexOf(current)+1);
            def newPrefix = prefix + "_" + ct;
            buf.append(buildNext(value, order, nextOrder, newPrefix, featureTypes));
            ct++;
        }
    }
    return buf.toString();
}

cart = ShoppingCartEvents.getCartObject(request);

// get the shopping lists for the user (if logged in)
if (userLogin) {
    exprList = [EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, userLogin.partyId),
                EntityCondition.makeCondition("listName", EntityOperator.NOT_EQUAL, "auto-save")];
    condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
    allShoppingLists = delegator.findList("ShoppingList", condition, null, ["listName"], null, false);
    context.shoppingLists = allShoppingLists;
}

// set the content path prefix
contentPathPrefix = CatalogWorker.getContentPathPrefix(request);
context.contentPathPrefix = contentPathPrefix;

// get the product detail information
if (product) {
    productId = product.productId;
    context.product_id = productId;
    productTypeId = product.productTypeId;

    boolean isMarketingPackage = EntityTypeUtil.hasParentType(delegator, "ProductType", "productTypeId", product.productTypeId, "parentTypeId", "MARKETING_PKG");
    context.isMarketingPackage = (isMarketingPackage? "true": "false");

    featureTypes = [:];
    featureOrder = [];

    // set this as a last viewed
    LAST_VIEWED_TO_KEEP = 10; // modify this to change the number of last viewed to keep
    lastViewedProducts = session.getAttribute("lastViewedProducts");
    if (!lastViewedProducts) {
        lastViewedProducts = [];
        session.setAttribute("lastViewedProducts", lastViewedProducts);
    }
    lastViewedProducts.remove(productId);
    lastViewedProducts.add(0, productId);
    while (lastViewedProducts.size() > LAST_VIEWED_TO_KEEP) {
        lastViewedProducts.remove(lastViewedProducts.size() - 1);
    }

    // make the productContentWrapper
    productContentWrapper = new ProductContentWrapper(product, request);
    context.productContentWrapper = productContentWrapper;

    // get the main detail image (virtual or single product)
    mainDetailImage = productContentWrapper.get("DETAIL_IMAGE_URL");
    if (mainDetailImage) {
        mainDetailImageUrl = ContentUrlTag.getContentPrefix(request) + mainDetailImage;
        context.mainDetailImageUrl = mainDetailImageUrl.toString();
    }

    // get next/previous information for category
    categoryId = parameters.category_id ?: product.primaryProductCategoryId;
    if (categoryId) context.categoryId = categoryId;

    catNextPreviousResult = null;
    if (categoryId) {
        prevNextMap = [categoryId : categoryId, productId : productId];
        prevNextMap.orderByFields = context.orderByFields ?: ["sequenceNum", "productId"];
        catNextPreviousResult = dispatcher.runSync("getPreviousNextProducts", prevNextMap);
        if (ServiceUtil.isError(catNextPreviousResult)) {
            request.setAttribute("errorMessageList", [ServiceUtil.getErrorMessage(catNextPreviousResult)]);
            return;
        }
        if (catNextPreviousResult && catNextPreviousResult.category) {
            context.category = catNextPreviousResult.category;
            context.previousProductId = catNextPreviousResult.previousProductId;
            context.nextProductId = catNextPreviousResult.nextProductId;
        }
    }

    // get the product store for only Sales Order not for Purchase Order.
    productStore = null;
    productStoreId = null;
    cart = ShoppingCartEvents.getCartObject(request);
    if (cart.isSalesOrder()) {
        productStore = ProductStoreWorker.getProductStore(request);
        productStoreId = productStore.productStoreId;
        context.productStoreId = productStoreId;
    }
    // get a defined survey
    productSurvey = ProductStoreWorker.getProductSurveys(delegator, productStoreId, productId, "CART_ADD");
    if (productSurvey) {
        survey = EntityUtil.getFirst(productSurvey);
        origParamMapId = UtilHttp.stashParameterMap(request);
        surveyContext = ["_ORIG_PARAM_MAP_ID_" : origParamMapId];
        surveyPartyId = userLogin?.partyId;
        wrapper = new ProductStoreSurveyWrapper(survey, surveyPartyId, surveyContext);
        context.surveyWrapper = wrapper;
    }

    // get the product price
    catalogId = CatalogWorker.getCurrentCatalogId(request);
    currentCatalogId = catalogId;
    webSiteId = CatalogWorker.getWebSiteId(request);
    autoUserLogin = request.getSession().getAttribute("autoUserLogin");
    if (cart.isSalesOrder()) {
        // sales order: run the "calculateProductPrice" service
        priceContext = [product : product, prodCatalogId : catalogId,
            currencyUomId : cart.getCurrency(), autoUserLogin : autoUserLogin];
        priceContext.webSiteId = webSiteId;
        priceContext.productStoreId = productStoreId;
        priceContext.checkIncludeVat = "Y";
        priceContext.agreementId = cart.getAgreementId();
        priceContext.partyId = cart.getPartyId();  // IMPORTANT: must put this in, or price will be calculated for the CSR instead of the customer
        priceMap = dispatcher.runSync("calculateProductPrice", priceContext);
        context.priceMap = priceMap;
    } else {
        // purchase order: run the "calculatePurchasePrice" service
        priceContext = [product : product, currencyUomId : cart.getCurrency(),
                partyId : cart.getPartyId(), userLogin : userLogin];
        priceMap = dispatcher.runSync("calculatePurchasePrice", priceContext);
        context.priceMap = priceMap;
    }

    // get the product review(s) 
    // get all product review in case of Purchase Order.
    reviewByAnd = [:];
    reviewByAnd.statusId = "PRR_APPROVED";
    if (cart.isSalesOrder()) {
        reviewByAnd.productStoreId = productStoreId;
    }
    reviews = product.getRelatedCache("ProductReview", reviewByAnd, ["-postedDateTime"]);
    context.productReviews = reviews;
    // get the average rating
    if (reviews) {
        ratingReviews = EntityUtil.filterByAnd(reviews, [EntityCondition.makeCondition("productRating", EntityOperator.NOT_EQUAL, null)]);
        if (ratingReviews) {
            context.averageRating = ProductWorker.getAverageProductRating(product, reviews, productStoreId);
            context.numRatings = ratingReviews.size();
        }
    }

    // get the days to ship
    // if order is purchase then don't calculate available inventory for product. 
    if (cart.isSalesOrder()) {
        facilityId = productStore.inventoryFacilityId;
        /*
        productFacility = delegator.findByPrimaryKeyCache("ProductFacility", [productId : productId, facilityId : facilityId);
        context.daysToShip = productFacility?.daysToShip
        */

        resultOutput = dispatcher.runSync("getInventoryAvailableByFacility", [productId : productId, facilityId : facilityId, useCache : false]);
        totalAvailableToPromise = resultOutput.availableToPromiseTotal;
        if (totalAvailableToPromise) {
            productFacility = delegator.findByPrimaryKeyCache("ProductFacility", [productId : productId, facilityId : facilityId]);
            context.daysToShip = productFacility?.daysToShip
        }
    } else {
       supplierProducts = delegator.findByAndCache("SupplierProduct", [productId : productId], ["-availableFromDate"]);
       supplierProduct = EntityUtil.getFirst(supplierProducts);
       if (supplierProduct?.standardLeadTimeDays) {
           standardLeadTimeDays = supplierProduct.standardLeadTimeDays;
           daysToShip = standardLeadTimeDays + 1;
           context.daysToShip = daysToShip;
       }
    }

    // get the product distinguishing features
    disFeatureMap = dispatcher.runSync("getProductFeatures", [productId : productId, type : "DISTINGUISHING_FEAT"]);
    disFeatureList = disFeatureMap.productFeatures;
    context.disFeatureList = disFeatureList;

    // an example of getting features of a certain type to show
    sizeProductFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", [productId : productId, productFeatureTypeId : "SIZE"], ["sequenceNum", "defaultSequenceNum"]);
    context.sizeProductFeatureAndAppls = sizeProductFeatureAndAppls;
    
    // get product variant for Box/Case/Each
    productVariants = [];
    boolean isAlternativePacking = ProductWorker.isAlternativePacking(delegator, product.productId, null);
    mainProducts = [];
    if(isAlternativePacking){
        productVirtualVariants = delegator.findByAndCache("ProductAssoc", UtilMisc.toMap("productIdTo", product.productId , "productAssocTypeId", "ALTERNATIVE_PACKAGE"));
        if(productVirtualVariants){
            productVirtualVariants.each { virtualVariantKey ->
                mainProductMap = [:];
                mainProduct = virtualVariantKey.getRelatedOneCache("MainProduct");
                quantityUom = mainProduct.getRelatedOneCache("QuantityUom");
                mainProductMap.productId = mainProduct.productId;
                mainProductMap.piecesIncluded = mainProduct.piecesIncluded;
                mainProductMap.uomDesc = quantityUom.description;
                mainProducts.add(mainProductMap);
            }
        }
    }
    context.mainProducts = mainProducts;
    
    // Special Variant Code
    if ("Y".equals(product.isVirtual)) {
        if ("VV_FEATURETREE".equals(ProductWorker.getProductVirtualVariantMethod(delegator, productId))) {
            context.featureLists = ProductWorker.getSelectableProductFeaturesByTypesAndSeq(product);
        } else {
            featureMap = dispatcher.runSync("getProductFeatureSet", [productId : productId]);
            featureSet = featureMap.featureSet;
            if (featureSet) {
                //if order is purchase then don't calculate available inventory for product.
                if (cart.isPurchaseOrder()) {
                    variantTreeMap = dispatcher.runSync("getProductVariantTree", [productId : productId, featureOrder : featureSet, checkInventory: false]);
                } else {
                    variantTreeMap = dispatcher.runSync("getProductVariantTree", [productId : productId, featureOrder : featureSet, productStoreId : productStoreId]);
                }
                variantTree = variantTreeMap.variantTree;
                imageMap = variantTreeMap.variantSample;
                virtualVariant = variantTreeMap.virtualVariant;
                context.virtualVariant = virtualVariant;
                if (variantTree) {
                    context.variantTree = variantTree;
                    context.variantTreeSize = variantTree.size();
                }
                unavailableVariants = variantTreeMap.unavailableVariants;
                if (unavailableVariants) {
                    context.unavailableVariants = unavailableVariants;
                }
                if (imageMap) {
                    context.variantSample = imageMap;
                    context.variantSampleKeys = imageMap.keySet();
                    context.variantSampleSize = imageMap.size();
                }
                context.featureSet = featureSet;

                if (variantTree) {
                    featureOrder = new LinkedList(featureSet);
                    featureOrder.each { featureKey ->
                        featureValue = delegator.findByPrimaryKeyCache("ProductFeatureType", [productFeatureTypeId : featureKey]);
                        fValue = featureValue.get("description") ?: featureValue.productFeatureTypeId;
                        featureTypes[featureKey] = fValue;
                    }
                }
                context.featureTypes = featureTypes;
                context.featureOrder = featureOrder;
                if (featureOrder) {
                    context.featureOrderFirst = featureOrder[0];
                }

                if (variantTree && imageMap) {
                    jsBuf = new StringBuffer();
                    jsBuf.append("<script language=\"JavaScript\" type=\"text/javascript\">");
                    jsBuf.append("var DET = new Array(" + variantTree.size() + ");");
                    jsBuf.append("var IMG = new Array(" + variantTree.size() + ");");
                    jsBuf.append("var OPT = new Array(" + featureOrder.size() + ");");
                    jsBuf.append("var VIR = new Array(" + virtualVariant.size() + ");");
                    jsBuf.append("var detailImageUrl = null;");
                    featureOrder.eachWithIndex { feature, i ->
                        jsBuf.append("OPT[" + i + "] = \"FT" + feature + "\";");
                    }
                    virtualVariant.eachWithIndex { variant, i ->
                        jsBuf.append("VIR[" + i + "] = \"" + variant + "\";");
                    }

                    // build the top level
                    topLevelName = featureOrder[0];
                    jsBuf.append("function list" + topLevelName + "() {");
                    jsBuf.append("document.forms[\"addform\"].elements[\"FT" + topLevelName + "\"].options.length = 1;");
                    jsBuf.append("document.forms[\"addform\"].elements[\"FT" + topLevelName + "\"].options[0] = new Option(\"" + featureTypes[topLevelName] + "\",\"\",true,true);");
                    if (variantTree) {
                        featureOrder.each { featureKey ->
                            jsBuf.append("document.forms[\"addform\"].elements[\"FT" + featureKey + "\"].options.length = 1;");
                        }
                        firstDetailImage = null;
                        firstLargeImage = null;
                        counter = 0;
                        variantTree.each { key, value ->
                            opt = null;
                            if (featureOrder.size() == 1) {
                                opt = value.iterator().next();
                            } else {
                                opt = counter as String;
                            }
                            // create the variant content wrapper
                            contentWrapper = new ProductContentWrapper(imageMap[key], request);

                            // initial image paths
                            detailImage = contentWrapper.get("DETAIL_IMAGE_URL") ?: productContentWrapper.get("DETAIL_IMAGE_URL");
                            largeImage = contentWrapper.get("LARGE_IMAGE_URL") ?: productContentWrapper.get("LARGE_IMAGE_URL");

                            // full image URLs
                            detailImageUrl = null;
                            largeImageUrl = null;

                            // append the content prefix
                            if (detailImage) {
                                detailImageUrl = (ContentUrlTag.getContentPrefix(request) + detailImage).toString();
                            }
                            if (largeImage) {
                                largeImageUrl = ContentUrlTag.getContentPrefix(request) + largeImage;
                            }

                            jsBuf.append("document.forms[\"addform\"].elements[\"FT" + topLevelName + "\"].options[" + (counter+1) + "] = new Option(\"" + key + "\",\"" + opt + "\");");
                            jsBuf.append("DET[" + counter + "] = \"" + detailImageUrl +"\";");
                            jsBuf.append("IMG[" + counter + "] = \"" + largeImageUrl +"\";");

                            if (!firstDetailImage) {
                                firstDetailImage = detailImageUrl;
                            }
                            if (!firstLargeImage) {
                                firstLargeImage = largeImage;
                            }
                            counter++;
                        }
                        context.firstDetailImage = firstDetailImage;
                        context.firstLargeImage = firstLargeImage;
                    }
                    jsBuf.append("}");

                    // build dynamic lists
                    if (variantTree) {
                        variantTree.values().eachWithIndex { varTree, topLevelKeysCt ->
                            cnt = "" + topLevelKeysCt;
                            if (varTree instanceof Map) {
                                jsBuf.append(buildNext(varTree, featureOrder, featureOrder[1], cnt, featureTypes));
                            }
                        }
                    }

                    // make a list of variant sku with requireAmount
                    variantsRes = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "PRODUCT_VARIANT", checkViewAllow : true, prodCatalogId : currentCatalogId]);
                    variants = variantsRes.assocProducts;
                    variantPriceList = [];
                    if (variants) {
                        amt = new StringBuffer();
                        amt.append("function checkAmtReq(sku) { ");
                        // Create the javascript to return the price for each variant
                        variantPriceJS = new StringBuffer();
                        variantPriceJS.append("function getVariantPrice(sku) { ");
                        // Format to apply the currency code to the variant price in the javascript
                        if (productStore) {
                            localeString = productStore.defaultLocaleString;
                            if (localeString) {
                                locale = UtilMisc.parseLocale(localeString);
                            }
                        }
                        numberFormat = NumberFormat.getCurrencyInstance(locale);
                        variants.each { variantAssoc ->
                            variant = variantAssoc.getRelatedOne("AssocProduct");
                            // Get the price for each variant. Reuse the priceContext already setup for virtual product above and replace the product
                            priceContext.product = variant;
                            if (cart.isSalesOrder()) {
                                // sales order: run the "calculateProductPrice" service
                                variantPriceMap = dispatcher.runSync("calculateProductPrice", priceContext);
                                BigDecimal calculatedPrice = (BigDecimal)variantPriceMap.get("price");
                                // Get the minimum quantity for variants if MINIMUM_ORDER_PRICE is set for variants.
                                variantPriceMap.put("minimumQuantity", ShoppingCart.getMinimumOrderQuantity(delegator, calculatedPrice, variant.get("productId")));
                                Iterator treeMapIter = variantTree.entrySet().iterator();
                                while (treeMapIter.hasNext()) {
                                    Map.Entry entry = treeMapIter.next();
                                    if (entry.getValue() instanceof  Map) {
                                        Iterator entryIter = entry.getValue().entrySet().iterator();
                                        while (entryIter.hasNext()) {
                                            Map.Entry innerentry = entryIter.next();
                                            if (variant.get("productId").equals(innerentry.getValue().get(0))) {
                                                variantPriceMap.put("variantName", innerentry.getKey());
                                                variantPriceMap.put("secondVariantName", entry.getKey());
                                                break;
                                            }
                                        }
                                    } else if (UtilValidate.isNotEmpty(entry.getValue())) { 
                                        if (variant.get("productId").equals(entry.getValue().get(0))) {
                                            variantPriceMap.put("variantName", entry.getKey());
                                            break;
                                        }
                                    }
                                }
                                variantPriceList.add(variantPriceMap);
                            } else {
                                variantPriceMap = dispatcher.runSync("calculatePurchasePrice", priceContext);
                            }
                            amt.append(" if (sku == \"" + variant.productId + "\") return \"" + (variant.requireAmount ?: "N") + "\"; ");
                            if (variantPriceMap && variantPriceMap.basePrice) {
                                variantPriceJS.append("  if (sku == \"" + variant.productId + "\") return \"" + numberFormat.format(variantPriceMap.basePrice) + "\"; ");
                            }
                            
                            // make a list of virtual variants sku with requireAmount
                            virtualVariantsRes = dispatcher.runSync("getAssociatedProducts", [productIdTo : variant.productId, type : "ALTERNATIVE_PACKAGE", checkViewAllow : true, prodCatalogId : currentCatalogId]);
                            virtualVariants = virtualVariantsRes.assocProducts;
                            
                            if(virtualVariants){
                                virtualVariants.each { virtualAssoc ->
                                    virtual = virtualAssoc.getRelatedOne("MainProduct");
                                    // Get price from a virtual product
                                    priceContext.product = virtual;
                                    if (cart.isSalesOrder()) {
                                        // sales order: run the "calculateProductPrice" service
                                        virtualPriceMap = dispatcher.runSync("calculateProductPrice", priceContext);
                                        BigDecimal calculatedPrice = (BigDecimal)virtualPriceMap.get("price");
                                        // Get the minimum quantity for variants if MINIMUM_ORDER_PRICE is set for variants.
                                        virtualPriceMap.put("minimumQuantity", ShoppingCart.getMinimumOrderQuantity(delegator, calculatedPrice, virtual.get("productId")));
                                        Iterator treeMapIter = variantTree.entrySet().iterator();
                                        while (treeMapIter.hasNext()) {
                                            Map.Entry entry = treeMapIter.next();
                                            if (entry.getValue() instanceof  Map) {
                                                Iterator entryIter = entry.getValue().entrySet().iterator();
                                                while (entryIter.hasNext()) {
                                                    Map.Entry innerentry = entryIter.next();
                                                    if (virtual.get("productId").equals(innerentry.getValue().get(0))) {
                                                        virtualPriceMap.put("variantName", innerentry.getKey());
                                                        virtualPriceMap.put("secondVariantName", entry.getKey());
                                                        break;
                                                    }
                                                }
                                            } else if (UtilValidate.isNotEmpty(entry.getValue())) { 
                                                if (virtual.get("productId").equals(entry.getValue().get(0))) {
                                                    virtualPriceMap.put("variantName", entry.getKey());
                                                    break;
                                                }
                                            }
                                        }
                                        variantPriceList.add(virtualPriceMap);
                                        variantPriceJS.append("  if (sku == \"" + virtual.productId + "\") return \"" + numberFormat.format(virtualPriceMap.basePrice) + "\"; ");
                                    } else {
                                        virtualPriceMap = dispatcher.runSync("calculatePurchasePrice", priceContext);
                                        variantPriceJS.append("  if (sku == \"" + virtual.productId + "\") return \"" + numberFormat.format(virtualPriceMap.price) + "\"; ");
                                    }
                                }
                                
                            }
                        }
                        amt.append(" } ");
                        variantPriceJS.append(" } ");
                    }
                    context.variantPriceList = variantPriceList;
                    jsBuf.append(amt.toString());
                    jsBuf.append(variantPriceJS.toString());
                    jsBuf.append("</script>");

                    context.virtualJavaScript = jsBuf;
                }
            }
        }
    } else {
        context.minimumQuantity= ShoppingCart.getMinimumOrderQuantity(delegator, priceMap.price, productId);
        if(isAlternativePacking){
            // get alternative product price when product doesn't have any feature 
            jsBuf = new StringBuffer();
            jsBuf.append("<script language=\"JavaScript\" type=\"text/javascript\">");
            
            // make a list of variant sku with requireAmount
            virtualVariantsRes = dispatcher.runSync("getAssociatedProducts", [productIdTo : productId, type : "ALTERNATIVE_PACKAGE", checkViewAllow : true, prodCatalogId : categoryId]);
            virtualVariants = virtualVariantsRes.assocProducts;
            // Format to apply the currency code to the variant price in the javascript
            if (productStore) {
                localeString = productStore.defaultLocaleString;
                if (localeString) {
                    locale = UtilMisc.parseLocale(localeString);
                }
            }
            virtualVariantPriceList = [];
            numberFormat = NumberFormat.getCurrencyInstance(locale);
            
            if(virtualVariants){
                amt = new StringBuffer();
                // Create the javascript to return the price for each variant
                variantPriceJS = new StringBuffer();
                variantPriceJS.append("function getVariantPrice(sku) { ");
                
                virtualVariants.each { virtualAssoc ->
                    virtual = virtualAssoc.getRelatedOne("MainProduct");
                    // Get price from a virtual product
                    priceContext.product = virtual;
                    if (cart.isSalesOrder()) {
                        // sales order: run the "calculateProductPrice" service
                        virtualPriceMap = dispatcher.runSync("calculateProductPrice", priceContext);
                        BigDecimal calculatedPrice = (BigDecimal)virtualPriceMap.get("price");
                        // Get the minimum quantity for variants if MINIMUM_ORDER_PRICE is set for variants.
                        virtualVariantPriceList.add(virtualPriceMap);
                        variantPriceJS.append("  if (sku == \"" + virtual.productId + "\") return \"" + numberFormat.format(virtualPriceMap.basePrice) + "\"; ");
                    } else {
                        virtualPriceMap = dispatcher.runSync("calculatePurchasePrice", priceContext);
                        variantPriceJS.append("  if (sku == \"" + virtual.productId + "\") return \"" + numberFormat.format(virtualPriceMap.price) + "\"; ");
                    }
                }
                variantPriceJS.append(" } ");
                
                context.virtualVariantPriceList = virtualVariantPriceList;
                jsBuf.append(amt.toString());
                jsBuf.append(variantPriceJS.toString());
                jsBuf.append("</script>");
                context.virtualVariantJavaScript = jsBuf;
            }
        }
    }

    availableInventory = 0.0;

    // if the product is a MARKETING_PKG_AUTO/PICK, then also get the quantity which can be produced from components
    if (isMarketingPackage) {
        resultOutput = dispatcher.runSync("getMktgPackagesAvailable", [productId : productId]);
        availableInventory = resultOutput.availableToPromiseTotal;
    } else {
        //get last inventory count from product facility for the product
        facilities = delegator.findList("ProductFacility", EntityCondition.makeCondition([productId : product.productId]), null, null, null, false)
        if(facilities) {
            facilities.each { facility ->
                lastInventoryCount = facility.lastInventoryCount;
                if (lastInventoryCount != null) {
                    availableInventory += lastInventoryCount;
                }
            }
        }
    }
    context.availableInventory = availableInventory;

    // get product associations
    alsoBoughtProducts = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "ALSO_BOUGHT", checkViewAllow : true, prodCatalogId : currentCatalogId, bidirectional : true, sortDescending : true]);
    context.alsoBoughtProducts = alsoBoughtProducts.assocProducts;

    obsoleteProducts = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "PRODUCT_OBSOLESCENCE", checkViewAllow : true, prodCatalogId : currentCatalogId]);
    context.obsoleteProducts = obsoleteProducts.assocProducts;

    crossSellProducts = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "PRODUCT_COMPLEMENT", checkViewAllow : true, prodCatalogId : currentCatalogId]);
    context.crossSellProducts = crossSellProducts.assocProducts;

    upSellProducts = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "PRODUCT_UPGRADE", checkViewAllow : true, prodCatalogId : currentCatalogId]);
    context.upSellProducts = upSellProducts.assocProducts;

    obsolenscenseProducts = dispatcher.runSync("getAssociatedProducts", [productIdTo : productId, type : "PRODUCT_OBSOLESCENCE", checkViewAllow : true, prodCatalogId : currentCatalogId]);
    context.obsolenscenseProducts = obsolenscenseProducts.assocProducts;

    accessoryProducts = dispatcher.runSync("getAssociatedProducts", [productId : productId, type : "PRODUCT_ACCESSORY", checkViewAllow : true, prodCatalogId : currentCatalogId]);
    context.accessoryProducts = accessoryProducts.assocProducts;

    // get other cross-sell information: product with a common feature
    commonProductFeatureId = "SYMPTOM";
    // does this product have that feature?
    commonProductFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", [productId : productId, productFeatureTypeId : commonProductFeatureId], ["sequenceNum", "defaultSequenceNum"]);
    if (commonProductFeatureAndAppls) {
        commonProductFeatureIds = EntityUtil.getFieldListFromEntityList(commonProductFeatureAndAppls, "productFeatureId", true);

        // now search for other products that have this feature
        visitId = VisitHandler.getVisitId(session);

        productSearchConstraintList = [];
        productSearchConstraintList.add(new ProductSearch.FeatureSetConstraint(commonProductFeatureIds));
        // make sure the view allow category is included
        productSearchConstraintList = ProductSearchSession.ensureViewAllowConstraint(productSearchConstraintList, currentCatalogId, delegator);

        // don't care about the sort on this one
        resultSortOrder = null;

        commonFeatureResultIdsOrig = ProductSearch.searchProducts(productSearchConstraintList, resultSortOrder, delegator, visitId);
        commonFeatureResultIds = [];
        commonFeatureResultIdIter = commonFeatureResultIdsOrig.iterator();
        while (commonFeatureResultIdIter.hasNext()) {
            commonFeatureResultId = commonFeatureResultIdIter.next();
            // filter out the current product
            if (commonFeatureResultId.equals(productId)) {
                continue;
            }
            // filter out all variants
            commonProduct = delegator.findByPrimaryKeyCache("Product", [productId : commonFeatureResultId]);
            if ("Y".equals(commonProduct?.isVariant)) {
                continue;
            }
            commonFeatureResultIds.add(commonFeatureResultId);
        }
        if (commonFeatureResultIds) {
            context.commonFeatureResultIds = commonFeatureResultIds;
        }
    }

    // get the DIGITAL_DOWNLOAD related Content records to show the contentName/description
    downloadProductContentAndInfoList = delegator.findByAndCache("ProductContentAndInfo", [productId : productId, productContentTypeId : "DIGITAL_DOWNLOAD"]);
    context.downloadProductContentAndInfoList = downloadProductContentAndInfoList;

    // not the best to save info in an action, but this is probably the best place to count a view; it is done async
    dispatcher.runAsync("countProductView", [productId : productId, weight : new Long(1)], false);

    //get product image from image management
    productImageList = [];
    productContentAndInfoImageManamentList = delegator.findByAnd("ProductContentAndInfo", ["productId": productId, productContentTypeId : "IMAGE", "statusId" : "IM_APPROVED", "drIsPublic" : "Y"], ["sequenceNum"]);
    if(productContentAndInfoImageManamentList) {
        productContentAndInfoImageManamentList.each { productContentAndInfoImageManament ->
            contentAssocThumbList = delegator.findByAnd("ContentAssoc", [contentId : productContentAndInfoImageManament.contentId, contentAssocTypeId : "IMAGE_THUMBNAIL"]);
            contentAssocThumb = EntityUtil.getFirst(contentAssocThumbList);
            if(contentAssocThumb) {
                imageContentThumb = delegator.findByPrimaryKey("Content", [contentId : contentAssocThumb.contentIdTo]);
                if(imageContentThumb) {
                    productImageThumb = delegator.findByPrimaryKey("ContentDataResourceView", [contentId : imageContentThumb.contentId, drDataResourceId : imageContentThumb.dataResourceId]);
                    productImageMap = [:];
                    productImageMap.productImageThumb = productImageThumb.drObjectInfo;
                    productImageMap.productImage = productContentAndInfoImageManament.drObjectInfo;
                    productImageList.add(productImageMap);
                }
            }
        }
        context.productImageList = productImageList;
    }
    
    // get reservation start date for rental product
    if("ASSET_USAGE".equals(productTypeId) || "ASSET_USAGE_OUT_IN".equals(productTypeId)){
        context.startDate = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), 1).toString().substring(0,10); // should be tomorrow.
    }
    
    // get product tags
    productKeywords = delegator.findByAnd("ProductKeyword", ["productId": productId, "keywordTypeId" : "KWT_TAG", "statusId" : "KW_APPROVED"]);
    keywordMap = [:];
    if (productKeywords) {
        for (productKeyword in productKeywords) {
            keywordConds = [EntityCondition.makeCondition("keyword", EntityOperator.EQUALS, productKeyword.keyword),
                            EntityCondition.makeCondition("keywordTypeId", EntityOperator.EQUALS, "KWT_TAG"),
                            EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "KW_APPROVED")];
                keywordCond = EntityCondition.makeCondition(keywordConds, EntityOperator.AND);
            productKeyWordCount = delegator.findCountByCondition("ProductKeyword", keywordCond, null, null);
            keywordMap.put(productKeyword.keyword,productKeyWordCount);
        }
        context.productTags = keywordMap;
    }
}
