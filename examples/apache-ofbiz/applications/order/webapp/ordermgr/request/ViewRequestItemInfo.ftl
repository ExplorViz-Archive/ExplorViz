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
        <div class="h3">${uiLabelMap.OrderRequestItems}</div>
    </div>
    <div class="screenlet-body">
        <table cellspacing="0" class="basic-table">
            <tr valign="bottom" class="header-row">
                <td width="10%">${uiLabelMap.ProductItem}</td>
                <td width="35%">${uiLabelMap.OrderProduct}</td>
                <td width="10%" align="right">${uiLabelMap.ProductQuantity}</td>
                <td width="10%" align="right">${uiLabelMap.OrderAmount}</td>
                <td width="10%" align="right">${uiLabelMap.OrderRequestMaximumAmount}</td>
                <td width="5%" align="right">&nbsp;</td>
            </tr>
            <#assign alt_row = false>
            <#list requestItems as requestItem>
                <#if requestItem.productId?exists>
                    <#assign product = requestItem.getRelatedOne("Product")>
                </#if>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                    <td valign="top">
                        <div>
                            <#if showRequestManagementLinks?exists>
                                <a href="<@ofbizUrl>EditRequestItem?custRequestId=${requestItem.custRequestId}&amp;custRequestItemSeqId=${requestItem.custRequestItemSeqId}</@ofbizUrl>" class="buttontext">${requestItem.custRequestItemSeqId}</a>
                            <#else>
                                ${requestItem.custRequestItemSeqId}
                            </#if>
                        </div>
                    </td>
                    <td valign="top">
                        <div>
                            ${(product.internalName)?if_exists}&nbsp;
                            <#if showRequestManagementLinks?exists>
                                <a href="/catalog/control/EditProduct?productId=${requestItem.productId?if_exists}" class="buttontext">${requestItem.productId?if_exists}</a>
                            <#else>
                                <a href="<@ofbizUrl>product?product_id=${requestItem.productId?if_exists}</@ofbizUrl>" class="buttontext">${requestItem.productId?if_exists}</a>
                            </#if>
                        </div>
                    </td>
                    <td align="right" valign="top">${requestItem.quantity?if_exists}</td>
                    <td align="right" valign="top">${requestItem.selectedAmount?if_exists}</td>
                    <td align="right" valign="top"><@ofbizCurrency amount=requestItem.maximumAmount isoCode=request.maximumAmountUomId/></td>
                </tr>
                <#-- toggle the row color -->
                <#assign alt_row = !alt_row>
            </#list>
        </table>
    </div>
</div>