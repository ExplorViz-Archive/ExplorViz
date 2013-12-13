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

<#macro updateOrderContactMech orderHeader contactMechTypeId contactMechList contactMechPurposeTypeId contactMechAddress>
  <#if (!orderHeader.statusId.equals("ORDER_COMPLETED")) && !(orderHeader.statusId.equals("ORDER_REJECTED")) && !(orderHeader.statusId.equals("ORDER_CANCELLED"))>
    <form name="updateOrderContactMech" method="post" action="<@ofbizUrl>updateOrderContactMech</@ofbizUrl>">
      <input type="hidden" name="orderId" value="${orderId?if_exists}" />
      <input type="hidden" name="contactMechPurposeTypeId" value="${contactMechPurpose.contactMechPurposeTypeId?if_exists}" />
      <input type="hidden" name="oldContactMechId" value="${contactMech.contactMechId?if_exists}" />
      <select name="contactMechId">
        <#if contactMech.contactMechTypeId == "POSTAL_ADDRESS">
          <option value="${contactMechAddress.contactMechId}">${(contactMechAddress.address1)?default("")} - ${contactMechAddress.city?default("")}</option>
          <option value="${contactMechAddress.contactMechId}"></option>
          <#list contactMechList as contactMech>
            <#assign postalAddress = contactMech.getRelatedOne("PostalAddress")?if_exists />
            <#assign partyContactPurposes = postalAddress.getRelated("PartyContactMechPurpose")?if_exists />
            <#list partyContactPurposes as partyContactPurpose>
              <#if contactMech.contactMechId?has_content && partyContactPurpose.contactMechPurposeTypeId == contactMechPurposeTypeId>
                <option value="${contactMech.contactMechId?if_exists}">${(postalAddress.address1)?default("")} - ${postalAddress.city?default("")}</option>
              </#if>
            </#list>
          </#list>
        <#elseif contactMech.contactMechTypeId == "TELECOM_NUMBER">
          <option value="${contactMechAddress.contactMechId}">${contactMechAddress.countryCode?if_exists} <#if contactMechAddress.areaCode?exists>${contactMechAddress.areaCode}-</#if>${contactMechAddress.contactNumber}</option>
          <option value="${contactMechAddress.contactMechId}"></option>
          <#list contactMechList as contactMech>
             <#assign telecomNumber = contactMech.getRelatedOne("TelecomNumber")?if_exists />
             <#assign partyContactPurposes = telecomNumber.getRelated("PartyContactMechPurpose")?if_exists />
             <#list partyContactPurposes as partyContactPurpose>
               <#if contactMech.contactMechId?has_content && partyContactPurpose.contactMechPurposeTypeId == contactMechPurposeTypeId>
                  <option value="${contactMech.contactMechId?if_exists}">${telecomNumber.countryCode?if_exists} <#if telecomNumber.areaCode?exists>${telecomNumber.areaCode}-</#if>${telecomNumber.contactNumber}</option>
               </#if>
             </#list>
          </#list>
        <#elseif contactMech.contactMechTypeId == "EMAIL_ADDRESS">
          <option value="${contactMechAddress.contactMechId}">${(contactMechAddress.infoString)?default("")}</option>
          <option value="${contactMechAddress.contactMechId}"></option>
          <#list contactMechList as contactMech>
             <#assign partyContactPurposes = contactMech.getRelated("PartyContactMechPurpose")?if_exists />
             <#list partyContactPurposes as partyContactPurpose>
               <#if contactMech.contactMechId?has_content && partyContactPurpose.contactMechPurposeTypeId == contactMechPurposeTypeId>
                  <option value="${contactMech.contactMechId?if_exists}">${contactMech.infoString?if_exists}</option>
               </#if>
             </#list>
          </#list>
        </#if>
      </select>
      <input type="submit" value="${uiLabelMap.CommonUpdate}" class="smallSubmit" />
    </form>
  </#if>
</#macro>

<#if displayParty?has_content || orderContactMechValueMaps?has_content>
<div class="screenlet">
    <div class="screenlet-title-bar">
      <ul><li class="h3">&nbsp;${uiLabelMap.OrderContactInformation}</li></ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
      <table class="basic-table" cellspacing='0'>
        <tr>
          <td align="right" valign="top" width="19%"><span class="label">&nbsp;${uiLabelMap.CommonName}</span></td>
          <td width="1%">&nbsp;</td>
          <td valign="top" width="80%">
            <div>
              <#if displayParty?has_content>
                <#assign displayPartyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", displayParty.partyId, "compareDate", orderHeader.orderDate, "userLogin", userLogin))/>
                ${displayPartyNameResult.fullName?default("[${uiLabelMap.OrderPartyNameNotFound}]")}
              </#if>
              <#if partyId?exists>
                &nbsp;(<a href="${customerDetailLink}${partyId}${externalKeyParam}" target="partymgr" class="buttontext">${partyId}</a>)
                <br/>
                <#if orderHeader.salesChannelEnumId != "POS_SALES_CHANNEL">
                <div>
                   <a href="<@ofbizUrl>/orderentry?partyId=${partyId}&amp;orderTypeId=${orderHeader.orderTypeId}</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderNewOrder}</a>
                   <a href="javascript:document.searchOtherOrders.submit()" class="buttontext">${uiLabelMap.OrderOtherOrders}</a>
                </div>
                  <form name="searchOtherOrders" method="post" action="<@ofbizUrl>searchorders</@ofbizUrl>">
                    <input type="hidden" name="lookupFlag" value="Y"/>
                    <input type="hidden" name="hideFields" value="Y"/>
                    <input type="hidden" name="partyId" value="${partyId}" />
                    <input type="hidden" name="viewIndex" value="1"/>
                    <input type="hidden" name="viewSize" value="20"/>
                  </form>
                </#if>
              </#if>
            </div>
          </td>
        </tr>
        <#list orderContactMechValueMaps as orderContactMechValueMap>
          <#assign contactMech = orderContactMechValueMap.contactMech>
          <#assign contactMechPurpose = orderContactMechValueMap.contactMechPurposeType>
          <tr><td colspan="3"><hr /></td></tr>
          <tr>
            <td align="right" valign="top" width="19%">
              <span class="label">&nbsp;${contactMechPurpose.get("description",locale)}</span>
            </td>
            <td width="1%">&nbsp;</td>
            <td valign="top" width="80%">
              <#if contactMech.contactMechTypeId == "POSTAL_ADDRESS">
                <#assign postalAddress = orderContactMechValueMap.postalAddress>
                <#if postalAddress?has_content>
                  <div>
                     ${setContextField("postalAddress", postalAddress)}
                     ${screens.render("component://party/widget/partymgr/PartyScreens.xml#postalAddressHtmlFormatter")}
                  </div>
                  <@updateOrderContactMech orderHeader=orderHeader?if_exists contactMechTypeId=contactMech.contactMechTypeId contactMechList=postalContactMechList?if_exists contactMechPurposeTypeId=contactMechPurpose.contactMechPurposeTypeId?if_exists contactMechAddress=postalAddress?if_exists />
                </#if>
              <#elseif contactMech.contactMechTypeId == "TELECOM_NUMBER">
                <#assign telecomNumber = orderContactMechValueMap.telecomNumber>
                <div>
                  ${telecomNumber.countryCode?if_exists}
                  <#if telecomNumber.areaCode?exists>${telecomNumber.areaCode}-</#if>${telecomNumber.contactNumber}
                  <#--<#if partyContactMech.extension?exists>ext&nbsp;${partyContactMech.extension}</#if>-->
                  <#if !telecomNumber.countryCode?exists || telecomNumber.countryCode == "011" || telecomNumber.countryCode == "1">
                    <a target="_blank" href="${uiLabelMap.CommonLookupAnywhoLink}" class="buttontext">${uiLabelMap.CommonLookupAnywho}</a>
                   <a target="_blank" href="${uiLabelMap.CommonLookupWhitepagesTelNumberLink}" class="buttontext">${uiLabelMap.CommonLookupWhitepages}</a>
                  </#if>
                </div>
                <@updateOrderContactMech orderHeader=orderHeader?if_exists contactMechTypeId=contactMech.contactMechTypeId contactMechList=telecomContactMechList?if_exists contactMechPurposeTypeId=contactMechPurpose.contactMechPurposeTypeId?if_exists contactMechAddress=telecomNumber?if_exists />
              <#elseif contactMech.contactMechTypeId == "EMAIL_ADDRESS">
                <div>
                  ${contactMech.infoString}
                  <#if security.hasEntityPermission("ORDERMGR", "_SEND_CONFIRMATION", session)>
                     (<a href="<@ofbizUrl>confirmationmailedit?orderId=${orderId}&amp;partyId=${partyId}&amp;sendTo=${contactMech.infoString}</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderSendConfirmationEmail}</a>)
                  <#else>
                     <a href="mailto:${contactMech.infoString}" class="buttontext">(${uiLabelMap.OrderSendEmail})</a>
                  </#if>
                </div>
                <@updateOrderContactMech orderHeader=orderHeader?if_exists contactMechTypeId=contactMech.contactMechTypeId contactMechList=emailContactMechList?if_exists contactMechPurposeTypeId=contactMechPurpose.contactMechPurposeTypeId?if_exists contactMechAddress=contactMech?if_exists />
              <#elseif contactMech.contactMechTypeId == "WEB_ADDRESS">
                <div>
                  ${contactMech.infoString}
                  <#assign openString = contactMech.infoString>
                  <#if !openString?starts_with("http") && !openString?starts_with("HTTP")>
                    <#assign openString = "http://" + openString>
                  </#if>
                  <a target="_blank" href="${openString}" class="buttontext">(open&nbsp;page&nbsp;in&nbsp;new&nbsp;window)</a>
                </div>
              <#else>
                <div>
                  ${contactMech.infoString?if_exists}
                </div>
              </#if>
            </td>
          </tr>
        </#list>
      </table>
    </div>
</div>
</#if>
