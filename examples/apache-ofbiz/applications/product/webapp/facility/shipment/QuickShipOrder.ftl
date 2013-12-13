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
<script language="JavaScript" type="text/javascript">
// function called from ShipmentScaleApplet when a weight is read
function setWeight(weight) {
  document.weightForm.weight.value = weight;
}
</script>

<#if security.hasEntityPermission("FACILITY", "_VIEW", session)>
  <h1>${uiLabelMap.ProductQuickShipOrderFrom} ${facility.facilityName?if_exists} [${uiLabelMap.CommonId}:${facilityId?if_exists}]</h1>
  <div class="button-bar">
    <a href="<@ofbizUrl>quickShipOrder?facilityId=${facilityId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNextShipment}</a>
    <#if shipment?has_content>
      <a href="<@ofbizUrl>EditShipment?shipmentId=${shipmentId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductEditShipment}</a>
    </#if>
  </div>

  <#if shipment?exists>
    <#if 1 < shipmentPackages.size()>
      <#-- multiple packages -->
      <div><font color="red">${uiLabelMap.ProductMorePackageFoundShipment}.</font></div>
    <#else>
      <#-- single package -->
      <#assign shipmentPackage = (Static["org.ofbiz.entity.util.EntityUtil"].getFirst(shipmentPackages))?if_exists>
      <#if shipmentPackage?has_content>
        <#assign weight = (shipmentPackage.weight)?default(0.00)>
        <#if (0 < weight?double) && !requestParameters.reweigh?exists>
          <#if 1 < shipmentRoutes.size()>
            <#-- multiple routes -->
            <div><font color="red">${uiLabelMap.ProductMoreRouteSegmentFound}.</font></div>
          <#elseif !requestParameters.shipmentRouteSegmentId?exists || requestAttributes._ERROR_MESSAGE_?exists>
            <form name="routeForm" method="post" action="<@ofbizUrl>setQuickRouteInfo</@ofbizUrl>">
              <#assign shipmentRoute = (Static["org.ofbiz.entity.util.EntityUtil"].getFirst(shipmentRoutes))?if_exists>
              <#assign carrierPerson = (shipmentRoute.getRelatedOne("CarrierPerson"))?if_exists>
              <#assign carrierPartyGroup = (shipmentRoute.getRelatedOne("CarrierPartyGroup"))?if_exists>
              <#assign shipmentMethodType = (shipmentRoute.getRelatedOne("ShipmentMethodType"))?if_exists>
              <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
              <input type="hidden" name="shipmentId" value="${shipmentRoute.shipmentId}"/>
              <input type="hidden" name="shipmentRouteSegmentId" value="${shipmentRoute.shipmentRouteSegmentId}"/>
              <table border="0" cellpadding="2" cellspacing="0">
                <tr>
                  <td width="20%" align="right"><span class="label">${uiLabelMap.ProductCarrier}</span></td>
                  <td>&nbsp;</td>
                  <td width="1%" nowrap="nowrap">
                    <select name="carrierPartyId">
                      <#if shipmentRoute.carrierPartyId?has_content>
                        <option value="${shipmentRoute.carrierPartyId}">${(carrierPerson.firstName)?if_exists} ${(carrierPerson.middleName)?if_exists} ${(carrierPerson.lastName)?if_exists} ${(carrierPartyGroup.groupName)?if_exists} [${shipmentRoute.carrierPartyId}]</option>
                        <option value="${shipmentRoute.carrierPartyId}">---</option>
                      <#else>
                        <option value="">&nbsp;</option>
                      </#if>
                      <#list carrierPartyDatas as carrierPartyData>
                        <option value="${carrierPartyData.party.partyId}">${(carrierPartyData.person.firstName)?if_exists} ${(carrierPartyData.person.middleName)?if_exists} ${(carrierPartyData.person.lastName)?if_exists} ${(carrierPartyData.partyGroup.groupName)?if_exists} [${carrierPartyData.party.partyId}]</option>
                      </#list>
                    </select>
                  </td>
                  <td>&nbsp;</td>
                  <td width="80%">
                    <a href="javascript:document.routeForm.submit();" class="buttontext">${uiLabelMap.ProductConfirmShipmentUps}</a>
                  </td>
                </tr>
                <tr>
                  <td width="20%" align="right"><span class="label">${uiLabelMap.ProductShipMethod}</span></td>
                  <td>&nbsp;</td>
                  <td width="1%" nowrap="nowrap">
                    <select name="shipmentMethodTypeId">
                      <#if shipmentMethodType?has_content>
                        <option value="${shipmentMethodType.shipmentMethodTypeId}">${shipmentMethodType.get("description",locale)}</option>
                        <option value="${shipmentMethodType.shipmentMethodTypeId}">---</option>
                      <#else>
                        <option value="">&nbsp;</option>
                      </#if>
                      <#list shipmentMethodTypes as shipmentMethodTypeOption>
                        <option value="${shipmentMethodTypeOption.shipmentMethodTypeId}">${shipmentMethodTypeOption.get("description",locale)}</option>
                      </#list>
                    </select>
                  </td>
                  <td>&nbsp;</td>
                  <td width="80%">
                    <a href="<@ofbizUrl>quickShipOrder?facilityId=${facilityId}&amp;shipmentId=${shipmentId}&amp;reweigh=Y</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductReWeighPackage}</a>
                  </td>
                </tr>
                <tr>
                  <td width="20%" align="right">&nbsp;</td>
                  <td>&nbsp;</td>
                  <td width="1%" nowrap="nowrap">
                    &nbsp;
                  </td>
                  <td>&nbsp;</td>
                  <td width="80%">
                    <input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onclick="javascript:document.routeForm.submit();" />
                  </td>
                </tr>
              </table>
            </form>
            <script language="JavaScript" type="text/javascript">
              document.routeForm.carrierPartyId.focus();
            </script>
          <#else>
            <#-- display the links for label/packing slip -->
            <#assign allDone = "yes">
            <center>
              <a href="<@ofbizUrl>viewShipmentPackageRouteSegLabelImage?shipmentId=${requestParameters.shipmentId}&amp;shipmentRouteSegmentId=${requestParameters.shipmentRouteSegmentId}&amp;shipmentPackageSeqId=00001</@ofbizUrl>" target="_blank" class="buttontext">${uiLabelMap.ProductShippingLabel}</a><br />
              <a href="<@ofbizUrl>ShipmentManifest.pdf?shipmentId=${requestParameters.shipmentId}&amp;shipmentRouteSegmentId=${requestParameters.shipmentRouteSegmentId}</@ofbizUrl>" target="_blank" class="buttontext">${uiLabelMap.ProductPackingSlip}</a>
            </center>
          </#if>
        <#else>
          <form name="weightForm" method="post" action="<@ofbizUrl>setQuickPackageWeight</@ofbizUrl>">
            <#assign weightUom = shipmentPackage.getRelatedOne("WeightUom")?if_exists>
            <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
            <input type="hidden" name="shipmentId" value="${shipmentPackage.shipmentId}"/>
            <input type="hidden" name="shipmentPackageSeqId" value="${shipmentPackage.shipmentPackageSeqId}"/>
            <table cellspacing="0" class="basic-table">
              <tr>
                <td width="20%" align="right"><span class="label">${uiLabelMap.ProductPackage}</span> ${shipmentPackage.shipmentPackageSeqId} ${uiLabelMap.ProductWeight}</td>
                <td>&nbsp;</td>
                <td width="80%">
                  <input type="text" name="weight" />&nbsp;
                  <select name="weightUomId">
                    <#if weightUom?has_content>
                      <option value="${weightUom.uomId}">${weightUom.get("description",locale)}</option>
                      <option value="${weightUom.uomId}">---</option>
                    </#if>
                    <#list weightUomList as weightUomOption>
                      <option value="${weightUomOption.uomId}">${weightUomOption.get("description",locale)} [${weightUomOption.abbreviation}]</option>
                    </#list>
                  </select>
                </td>
              </tr>
              <tr>
                <td colspan="2">&nbsp;</td>
                <td width="80%">
                  <input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onclick="javascript:document.weightForm.submit();"/>
                  <a href="javascript:document.weightForm.submit();" class="buttontext">${uiLabelMap.ProductSetWeight}</a>
                </td>
              </tr>
            </table>
          </form>
          <script language="JavaScript" type="text/javascript">
            document.weightForm.weight.focus();
          </script>
          <#-- todo embed the applet
          <applet code="ShipmentScaleApplet.class" codebase="/images/" name="Package Weight Reader" width="0" height="0" MAYSCRIPT>
            <param name="serialPort" value="com1">
            <param name="fakeWeight" value="22">
          </applet>
          -->
        </#if>
      <#else>
        <div class="alert">${uiLabelMap.ProductErrorNoPackagesFoundForShipment} !</div>
      </#if>
      <hr />
      ${pages.get("/shipment/ViewShipmentInfo.ftl")}
      <br />${pages.get("/shipment/ViewShipmentItemInfo.ftl")}
      <br />${pages.get("/shipment/ViewShipmentPackageInfo.ftl")}
      <#if allDone?default("no") == "yes">
        <br />${pages.get("/shipment/ViewShipmentRouteInfo.ftl")}
      </#if>
    </#if>
  <#else>
    <form name="selectOrderForm" method="post" action="<@ofbizUrl>createQuickShipment</@ofbizUrl>">
      <input type="hidden" name="facilityId" value="${facilityId?if_exists}" />
      <input type="hidden" name="originFacilityId" value="${facilityId?if_exists}" />
      <input type="hidden" name="setPackedOnly" value="Y" />
      <table border='0' cellpadding='2' cellspacing='0'>
        <tr>
          <td width="25%" align='right'><span class="label">${uiLabelMap.ProductOrderNumber}</span></td>
          <td width="1">&nbsp;</td>
          <td width="25%">
            <input type="text" name="orderId" size="20" maxlength="20" value="${requestParameters.orderId?if_exists}" />
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td colspan="2">&nbsp;</td>
          <td colspan="2">
            <input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onclick="javascript:document.selectOrderForm.submit();" />
            <a href="javascript:document.selectOrderForm.submit();" class="buttontext">${uiLabelMap.ProductShipOrder}</a>
          </td>
        </tr>
      </table>
    </form>
    <script language="JavaScript" type="text/javascript">
        document.selectOrderForm.orderId.focus();
    </script>
  </#if>
</#if>
