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
<div class="screenlet">
    <div class="screenlet-title-bar">
        <div class="boxlink">
            <#if maySelectItems?default("N") == "Y">
                <a href="javascript:document.addCommonToCartForm.add_all.value='true';document.addCommonToCartForm.submit()" class="buttontext">${uiLabelMap.OrderAddAllToCart}</a>
            </#if>
        </div>
        <div class="h3">${uiLabelMap.OrderOrderQuoteItems}</div>
    </div>
    <div class="screenlet-body">
        <table cellspacing="0" class="basic-table">
            <tr valign="bottom" class="header-row">
                <td width="15%">${uiLabelMap.ProductItem}</td>
                <td width="20%">${uiLabelMap.ProductProduct}</td>
                <td width="10%" align="right">${uiLabelMap.ProductQuantity}</td>
                <td width="10%" align="right">${uiLabelMap.OrderSelAmount}</td>
                <td width="5%" align="right">&nbsp;</td>
                <td width="10%" align="right">${uiLabelMap.OrderOrderQuoteUnitPrice}</td>
                <td width="10%" align="right">${uiLabelMap.OrderAdjustments}</td>
                <td width="10%" align="right">${uiLabelMap.CommonSubtotal}</td>
            </tr>
            <tr valign="bottom" class="header-row">
                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${uiLabelMap.OrderOrderTermType}</td>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${uiLabelMap.OrderOrderTermValue}</td>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${uiLabelMap.OrderOrderTermDays}</td>
                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${uiLabelMap.OrderQuoteTermDescription}</td>
                <td></td>
                <td></td>
                <td></td>
                <td align="right">&nbsp;</td>
            </tr>
            <#assign totalQuoteAmount = 0.0>
            <#assign alt_row = false/>
            <#list quoteItems as quoteItem>
                <#if quoteItem.productId?exists>
                    <#assign product = quoteItem.getRelatedOne("Product")>
                <#else>
                    <#assign product = null> <#-- don't drag it along to the next iteration -->
                </#if>
                <#assign selectedAmount = quoteItem.selectedAmount?default(1)>
                <#if selectedAmount == 0>
                    <#assign selectedAmount = 1/>
                </#if>
                <#assign quoteItemAmount = quoteItem.quoteUnitPrice?default(0) * quoteItem.quantity?default(0) * selectedAmount>
                <#assign quoteItemAdjustments = quoteItem.getRelated("QuoteAdjustment")>
                <#assign totalQuoteItemAdjustmentAmount = 0.0>
                <#list quoteItemAdjustments as quoteItemAdjustment>
                    <#assign totalQuoteItemAdjustmentAmount = quoteItemAdjustment.amount?default(0) + totalQuoteItemAdjustmentAmount>
                </#list>
                <#assign totalQuoteItemAmount = quoteItemAmount + totalQuoteItemAdjustmentAmount>
                <#assign totalQuoteAmount = totalQuoteAmount + totalQuoteItemAmount>
                
                <tr <#if alt_row>class="alternate-row" </#if>>
                    <td >
                        <div>
                        <#if showQuoteManagementLinks?exists && quoteItem.isPromo?default("N") == "N" && quote.statusId=="QUO_CREATED">
                            <a href="<@ofbizUrl>EditQuoteItem?quoteId=${quoteItem.quoteId}&amp;quoteItemSeqId=${quoteItem.quoteItemSeqId}</@ofbizUrl>" class="buttontext">${quoteItem.quoteItemSeqId}</a>
                        <#else>
                            ${quoteItem.quoteItemSeqId}
                        </#if>
                        </div>
                        <#assign quoteTerms = delegator.findByAnd("QuoteTerm", {"quoteId" : quoteItem.quoteId, "quoteItemSeqId" : quoteItem.quoteItemSeqId})>
                    </td>
                    <td valign="top">
                        <div>
                            ${(product.internalName)?if_exists}&nbsp;
                            <#if showQuoteManagementLinks?exists>
                                <a href="/catalog/control/EditProduct?productId=${quoteItem.productId?if_exists}" class="buttontext">
                                  <#if quoteItem.productId?exists>
                                    ${quoteItem.productId}
                                  <#else>
                                    ${uiLabelMap.ProductCreateProduct}
                                  </#if>
                                </a>
                            <#else>
                                <a href="<@ofbizUrl>product?product_id=${quoteItem.productId?if_exists}</@ofbizUrl>" class="buttontext">${quoteItem.productId?if_exists}</a>
                            </#if>
                        </div>
                    </td>
                    <td></td>
                    <td align="right" valign="top">${quoteItem.quantity?if_exists}</td>
                    <td align="right" valign="top">${quoteItem.selectedAmount?if_exists}</td>
                    <td align="right" valign="top"><@ofbizCurrency amount=quoteItem.quoteUnitPrice isoCode=quote.currencyUomId/></td>
                    <td align="right" valign="top"><@ofbizCurrency amount=totalQuoteItemAdjustmentAmount isoCode=quote.currencyUomId/></td>
                    <td align="right" valign="top"><@ofbizCurrency amount=totalQuoteItemAmount isoCode=quote.currencyUomId/></td>
                </tr>
                <#list quoteTerms as quoteTerm>
                <#assign termDescription = delegator.findByPrimaryKey("TermType",{"termTypeId":quoteTerm.termTypeId})>
                <tr <#if alt_row>class="alternate-row" </#if>>
                    <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${termDescription.description?if_exists}</td>
                    <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${quoteTerm.termValue?if_exists}</td>
                    <td valign="top"><#if quoteTerm.termDays?exists>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${quoteTerm.termDays?if_exists}</#if></td>
                    <td valign="top"><#if quoteTerm.description?exists>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${quoteTerm.description}</#if></td>
                    <td align="right" valign="top"></td>
                    <td align="right" valign="top"></td>
                    <td align="right" valign="top"></td>
                    <td align="right" valign="top"></td>
                </tr>
                </#list>
                <#-- now show adjustment details per line item -->
                <#list quoteItemAdjustments as quoteItemAdjustment>
                    <#assign adjustmentType = quoteItemAdjustment.getRelatedOne("OrderAdjustmentType")>
                    <tr class="alternate-row">
                        <td align="right" colspan="4"><span class="label">${adjustmentType.get("description",locale)?if_exists}</span></td>
                        <td align="right"><@ofbizCurrency amount=quoteItemAdjustment.amount isoCode=quote.currencyUomId/></td>
                        <td>&nbsp;</td>
                    </tr>
                </#list>
                <#-- toggle the row color -->
                <#assign alt_row = !alt_row>
            </#list>
            <tr><td colspan="10"><hr /></td></tr>
            <tr>
                <td align="right" colspan="7" class="label">${uiLabelMap.CommonSubtotal}</td>
                <td align="right"><@ofbizCurrency amount=totalQuoteAmount isoCode=quote.currencyUomId/></td>
            </tr>
            <tr><td colspan="5"></td><td colspan="6"><hr /></td></tr>
            <#assign totalQuoteHeaderAdjustmentAmount = 0.0>
            <#assign findAdjustment = false>
            <#list quoteAdjustments as quoteAdjustment>
                <#assign adjustmentType = quoteAdjustment.getRelatedOne("OrderAdjustmentType")>
                <#if !quoteAdjustment.quoteItemSeqId?exists>
                    <#assign totalQuoteHeaderAdjustmentAmount = quoteAdjustment.amount?default(0) + totalQuoteHeaderAdjustmentAmount>
                    <tr>
                      <td align="right" colspan="6"><span class="label">${adjustmentType.get("description",locale)?if_exists}</span></td>
                      <td align="right"><@ofbizCurrency amount=quoteAdjustment.amount isoCode=quote.currencyUomId/></td>
                    </tr>
                </#if>
                <#assign findAdjustment = true>
            </#list>
            <#assign grandTotalQuoteAmount = totalQuoteAmount + totalQuoteHeaderAdjustmentAmount>
            <#if findAdjustment>
            <tr><td colspan="5"></td><td colspan="6"><hr /></td></tr>
            </#if>
            <tr>
                <td align="right" colspan="7" class="label">${uiLabelMap.OrderGrandTotal}</td>
                <td align="right">
                    <@ofbizCurrency amount=grandTotalQuoteAmount isoCode=quote.currencyUomId/>
                </td>
            </tr>
        </table>
    </div>
</div>
