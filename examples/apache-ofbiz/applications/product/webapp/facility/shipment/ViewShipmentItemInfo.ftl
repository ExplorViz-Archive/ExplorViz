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
<#if shipmentItemDatas?has_content>
<div class="screenlet">
    <div class="screenlet-body">
      <table cellspacing="0" cellpadding="2" class="basic-table">
        <tr class="header-row">
          <td>${uiLabelMap.ProductItem}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>${uiLabelMap.ProductQuantity}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <#assign alt_row = false>
        <#list shipmentItemDatas as shipmentItemData>
            <#assign shipmentItem = shipmentItemData.shipmentItem>
            <#assign itemIssuances = shipmentItemData.itemIssuances>
            <#assign orderShipments = shipmentItemData.orderShipments>
            <#assign shipmentPackageContents = shipmentItemData.shipmentPackageContents>
            <#assign product = shipmentItemData.product?if_exists>
            <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                <td>${shipmentItem.shipmentItemSeqId}</td>
                <td colspan="2">${(product.internalName)?if_exists} <a href="/catalog/control/EditProduct?productId=${shipmentItem.productId?if_exists}" class="buttontext">${shipmentItem.productId?if_exists}</a></td>
                <td>${shipmentItem.quantity?default("&nbsp;")}</td>
                <td colspan="2">${shipmentItem.shipmentContentDescription?default("&nbsp;")}</td>
            </tr>
            <#list orderShipments as orderShipment>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                    <td>&nbsp;</td>
                    <td><span class="label">${uiLabelMap.ProductOrderItem}</span> <a href="/ordermgr/control/orderview?orderId=${orderShipment.orderId?if_exists}&amp;externalLoginKey=${requestAttributes.externalLoginKey}" class="buttontext">${orderShipment.orderId?if_exists}</a>${orderShipment.orderItemSeqId?if_exists}</td>
                    <td>&nbsp;</td>
                    <td>${orderShipment.quantity?if_exists}</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>
            </#list>
            <#list itemIssuances as itemIssuance>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                    <td>&nbsp;</td>
                    <td><span class="label">${uiLabelMap.ProductOrderItem}</span> <a href="/ordermgr/control/orderview?orderId=${itemIssuance.orderId?if_exists}&amp;externalLoginKey=${requestAttributes.externalLoginKey}" class="buttontext">${itemIssuance.orderId?if_exists}</a>${itemIssuance.orderItemSeqId?if_exists}</td>
                    <td><span class="label">${uiLabelMap.ProductInventory}</span> <a href="<@ofbizUrl>EditInventoryItem?inventoryItemId=${itemIssuance.inventoryItemId?if_exists}</@ofbizUrl>" class="buttontext">${itemIssuance.inventoryItemId?if_exists}</a></td>
                    <td>${itemIssuance.quantity?if_exists}</td>
                    <td>${itemIssuance.issuedDateTime?if_exists}</td>
                    <td class="label">${uiLabelMap.ProductFuturePartyRoleList}</td>
                </tr>
            </#list>
            <#list shipmentPackageContents as shipmentPackageContent>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                    <td>&nbsp;</td>
                    <td colspan="2"><span class="label">${uiLabelMap.ProductPackage}</span> ${shipmentPackageContent.shipmentPackageSeqId}</td>
                    <td>${shipmentPackageContent.quantity?if_exists}</td>
                    <td colspan="2">&nbsp;</td>
                </tr>
            </#list>
            <#-- toggle the row color -->
            <#assign alt_row = !alt_row>
        </#list>
      </table>
    </div>
</div>
</#if>