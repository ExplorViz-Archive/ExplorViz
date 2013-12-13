<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#if product?exists>
  <span class="pid">
    <div>
      <b>${product.productId}</b>
    </div>
  </span>
  <span class="name">
    <div>
      <a href="<@ofbizUrl>product?product_id=${product.productId}</@ofbizUrl>" class="buttontext">${productContentWrapper.get("PRODUCT_NAME")?if_exists}</a>
    </div>
  </span>
  <span class="listPrice">
    <div>
      <#if price.listPrice?exists && price.price?exists && price.price?double < price.listPrice?double>
        ${uiLabelMap.ProductListPrice}: <@ofbizCurrency amount=price.listPrice isoCode=price.currencyUsed/>
      <#else>
        &nbsp;
      </#if>
    </div>
  </span>
  <span class="totalPrice">
    <#if totalPrice?exists>
        <div>${uiLabelMap.ProductAggregatedPrice}: <span class='basePrice'><@ofbizCurrency amount=totalPrice isoCode=totalPrice.currencyUsed/></span></div>
    <#else>
      <div class="<#if price.isSale?exists && price.isSale>salePrice<#else>normalPrice</#if>">
        <b><@ofbizCurrency amount=price.price isoCode=price.currencyUsed/></b>
      </div>
    </#if>
  </span>
  <span class="qty">
    <#-- check to see if introductionDate hasn't passed yet -->
    <#if product.introductionDate?exists && nowTimestamp.before(product.introductionDate)>
      <div style="color: red;">${uiLabelMap.ProductNotYetAvailable}</div>
    <#-- check to see if salesDiscontinuationDate has passed -->
    <#elseif product.salesDiscontinuationDate?exists && nowTimestamp.before(product.salesDiscontinuationDate)>
      <div style="color: red;">${uiLabelMap.ProductNoLongerAvailable}</div>
    <#-- check to see if the product is a virtual product -->
    <#elseif product.isVirtual?default("N") == "Y">
      <div>
        <a href="<@ofbizUrl>product?<#if categoryId?exists>category_id=${categoryId}&amp;</#if>product_id=${product.productId}</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderChooseVariations}...</a>
      </div>
    <#else>
      <div>
        <input type="text" size="5" name="quantity_${product.productId}" value="" />
      </div>
    </#if>
  </span>
<#else>
  <h1>${uiLabelMap.ProductErrorProductNotFound}.</h1>
</#if>


