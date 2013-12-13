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

productPromoCodeId = request.getParameter("productPromoCodeId");
if (!productPromoCodeId) {
    productPromoCodeId = request.getAttribute("productPromoCodeId");
}
productPromoCode = delegator.findOne("ProductPromoCode", [productPromoCodeId : productPromoCodeId], false);

productPromoId = null;
if (productPromoCode) {
    productPromoId = productPromoCode.productPromoId;
} else {
    productPromoId = request.getParameter("productPromoId");
}

productPromo = null;
if (productPromoId) {
    productPromo = delegator.findOne("ProductPromo", [productPromoId : productPromoId], false);
}

productPromoCodeEmails = null;
productPromoCodeParties = null;
if (productPromoCode) {
    productPromoCodeEmails = productPromoCode.getRelated("ProductPromoCodeEmail");
    productPromoCodeParties = productPromoCode.getRelated("ProductPromoCodeParty");
}

context.productPromoId = productPromoId;
context.productPromo = productPromo;
context.productPromoCodeId = productPromoCodeId;
context.productPromoCode = productPromoCode;
context.productPromoCodeEmails = productPromoCodeEmails;
context.productPromoCodeParties = productPromoCodeParties;
