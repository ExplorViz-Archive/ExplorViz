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

<div>
  <#assign shoppingCart = sessionAttributes.shoppingCart?if_exists />
  <h2>${uiLabelMap.OrderCheckout}</h2>
    <#if shoppingCart?has_content && shoppingCart.size() &gt; 0>
      <div id="checkoutPanel">

<#-- ========================================================================================================================== -->
        <div id="cartPanel" class="screenlet">
          ${screens.render("component://ecommerce/widget/CartScreens.xml#UpdateCart")}
        </div>

<#-- ========================================================================================================================== -->
        <div id="shippingPanel" class="screenlet">
          <h3>${uiLabelMap.EcommerceStep} 2: ${uiLabelMap.FacilityShipping}</h3>
          <div id="shippingSummaryPanel" style="display: none;">
            <a href="javascript:void(0);" id="openShippingPanel" class="button">${uiLabelMap.EcommerceClickHereToEdit}</a>
            <div id="shippingCompleted">
              <ul>
                <li>
                  <h4>${uiLabelMap.OrderShipTo}</h4>
                  <ul>
                    <li id="completedShipToAttn"></li>
                    <li id="completedShippingContactNumber"></li>
                    <li id="completedEmailAddress"></li>
                  </ul>
                </li>
                <li>
                  <h4>${uiLabelMap.EcommerceLocation}</h4>
                  <ul>
                    <li id="completedShipToAddress1"></li>
                    <li id="completedShipToAddress2"></li>
                    <li id="completedShipToGeo"></li>
                  </ul>
                </li>
              </ul>
            </div>
          </div>

<#-- ============================================================= -->
          <div id="editShippingPanel" style="display: none;">
            <form id="shippingForm" action="<@ofbizUrl>createUpdateShippingAddress</@ofbizUrl>" method="post">
                <fieldset>
                  <input type="hidden" id="shipToContactMechId" name="shipToContactMechId" value="${shipToContactMechId?if_exists}" />
                  <input type="hidden" id="billToContactMechIdInShipingForm" name="billToContactMechId" value="${billToContactMechId?if_exists}" />
                  <input type="hidden" id="shipToPartyId" name="partyId" value="${partyId?if_exists}" />
                  <input type="hidden" id="shipToPhoneContactMechId" name="shipToPhoneContactMechId" value="${(shipToTelecomNumber.contactMechId)?if_exists}" />
                  <input type="hidden" id="emailContactMechId" name="emailContactMechId" value="${emailContactMechId?if_exists}" />
                  <input type="hidden" name="shipToName" value="${shipToName?if_exists}" />
                  <input type="hidden" name="shipToAttnName" value="${shipToAttnName?if_exists}" />
                  <#if userLogin?exists>
                    <input type="hidden" name="keepAddressBook" value="Y" />
                    <input type="hidden" name="setDefaultShipping" value="Y" />
                    <input type="hidden" name="userLoginId" id="userLoginId" value="${userLogin.userLoginId?if_exists}" />
                    <#assign productStoreId = Static["org.ofbiz.product.store.ProductStoreWorker"].getProductStoreId(request) />
                    <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}" />
                  <#else>
                    <input type="hidden" name="keepAddressBook" value="N" />
                  </#if>
                  <div id="shippingFormServerError" class="errorMessage"></div>
                  <div>
                      <span>
                        <label for="firstName">${uiLabelMap.PartyFirstName}*
                          <span id="advice-required-firstName" style="display: none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                        </label>
                        <input id="firstName" name="firstName" class="required" type="text" value="${firstName?if_exists}" />
                      </span>
                      <span>
                        <label for="lastName">${uiLabelMap.PartyLastName}*
                          <span id="advice-required-lastName" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                        </label>
                        <input id="lastName" name="lastName" class="required" type="text" value="${lastName?if_exists}" />
                      </span>
                  </div>
                  <div>
                  <#if shipToTelecomNumber?has_content>
                      <span>
                          <label for="shipToCountryCode">${uiLabelMap.CommonCountry}*
                              <span id="advice-required-shipToCountryCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToCountryCode" class="required" id="shipToCountryCode" value="${shipToTelecomNumber.countryCode?if_exists}" size="5" maxlength="10" /> -
                      </span>
                      <span>
                          <label for="shipToAreaCode">${uiLabelMap.PartyAreaCode}*
                              <span id="advice-required-shipToAreaCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToAreaCode" class="required" id="shipToAreaCode" value="${shipToTelecomNumber.areaCode?if_exists}" size="5" maxlength="10" /> -
                      </span>
                      <span>
                          <label for="shipToContactNumber">${uiLabelMap.PartyContactNumber}*
                              <span id="advice-required-shipToContactNumber" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToContactNumber" class="required" id="shipToContactNumber" value="${shipToTelecomNumber.contactNumber?if_exists}" size="10" maxlength="15" /> -
                      </span>
                      <span>
                          <label for="shipToExtension">${uiLabelMap.PartyExtension}</label>
                          <input type="text" name="shipToExtension" id="shipToExtension" value="${shipToExtension?if_exists}" size="5" maxlength="10" />
                      </span>
                  <#else>
                      <span>
                          <label for="shipToCountryCode">${uiLabelMap.CommonCountry}*
                              <span id="advice-required-shipToCountryCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToCountryCode" class="required" id="shipToCountryCode" value="${parameters.shipToCountryCode?if_exists}" size="5" maxlength="10" /> -
                      </span>
                      <span>
                          <label for="shipToAreaCode">${uiLabelMap.PartyAreaCode}*
                              <span id="advice-required-shipToAreaCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToAreaCode" class="required" id="shipToAreaCode" value="${parameters.shipToAreaCode?if_exists}" size="5" maxlength="10" /> -
                      </span>
                      <span>
                          <label for="shipToContactNumber">${uiLabelMap.PartyContactNumber}*
                              <span id="advice-required-shipToContactNumber" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input type="text" name="shipToContactNumber" class="required" id="shipToContactNumber" value="${parameters.shipToContactNumber?if_exists}" size="10" maxlength="15" /> -
                      </span>
                      <span>
                          <label for="shipToExtension">${uiLabelMap.PartyExtension}</label>
                          <input type="text" name="shipToExtension" id="shipToExtension" value="${parameters.shipToExtension?if_exists}" size="5" maxlength="10" />
                      </span>
                  </#if>
                  </div>
                  <div>
                      <span>
                          <label for="emailAddress">${uiLabelMap.PartyEmailAddress}*
                            <span id="advice-required-emailAddress" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                          </label>
                          <input id="emailAddress" name="emailAddress" class="required validate-email" maxlength="255" size="40" type="text" value="${emailAddress?if_exists}" />
                      </span>
                  </div>
                    <div>
                        <span>
                            <label for="shipToAddress1">${uiLabelMap.PartyAddressLine1}*
                                <span id="advice-required-shipToAddress1" class="custom-advice errorMessage" style="display:none"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <input id="shipToAddress1" name="shipToAddress1" class="required" type="text" value="${shipToAddress1?if_exists}" maxlength="255" size="40" />
                        </span>
                    </div>
                    <div>
                        <span>
                          <label for="shipToAddress2">${uiLabelMap.PartyAddressLine2}</label>
                          <input id="shipToAddress2" name="shipToAddress2" type="text" value="${shipToAddress2?if_exists}" maxlength="255" size="40" />
                        </span>
                    </div>
                    <div>
                        <span>
                            <label for="shipToCity">${uiLabelMap.CommonCity}*
                                <span id="advice-required-shipToCity" class="custom-advice errorMessage" style="display:none"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <input id="shipToCity" name="shipToCity" class="required" type="text" value="${shipToCity?if_exists}" maxlength="255" size="40" />
                        </span>
                    </div>
                    <div>
                        <span>
                            <label for="shipToPostalCode">${uiLabelMap.PartyZipCode}*
                                <span id="advice-required-shipToPostalCode" class="custom-advice errorMessage" style="display:none"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <input id="shipToPostalCode" name="shipToPostalCode" class="required" type="text" value="${shipToPostalCode?if_exists}" size="12" maxlength="10" />
                        </span>
                    </div>
                    <div>
                        <span>
                            <label for="shipToCountryGeoId">${uiLabelMap.CommonCountry}*
                                <span id="advice-required-shipToCountryGeo" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <select name="shipToCountryGeoId" id="shipToCountryGeoId">
                              <#if shipToCountryGeoId?exists>
                                <option value="${shipToCountryGeoId?if_exists}">${shipToCountryProvinceGeo?default(shipToCountryGeoId?if_exists)}</option>
                              </#if>
                              ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                            </select>
                        </span>
                    </div>
                    <div id="shipToStates">
                        <span>
                            <label for="shipToStateProvinceGeoId">${uiLabelMap.CommonState}*
                                <span id="advice-required-shipToStateProvinceGeoId" style="display:none" class="errorMessage">(${uiLabelMap.CommonRequired})</span>
                            </label>
                            <select id="shipToStateProvinceGeoId" name="shipToStateProvinceGeoId">
                              <#if shipToStateProvinceGeoId?has_content>
                                <option value='${shipToStateProvinceGeoId?if_exists}'>${shipToStateProvinceGeo?default(shipToStateProvinceGeoId?if_exists)}</option>
                              <#else>
                                <option value="_NA_">${uiLabelMap.PartyNoState}</option>
                              </#if>
                              ${screens.render("component://common/widget/CommonScreens.xml#states")}
                            </select>
                        </span>
                    </div>
                  </fieldset>
                  <fieldset>
                    <a href="javascript:void(0);" class="button" id="savePartyAndShippingContact">${uiLabelMap.EcommerceContinueToStep} 3</a>
                    <a style="display:none" class="button" href="javascript:void(0);" id="processingShippingOptions">${uiLabelMap.EcommercePleaseWait}....</a>
                  </fieldset>
            </form>
          </div>
        </div>

<#-- ========================================================================================================================== -->
        <div id="shippingOptionPanel" class="screenlet">
          <h3>${uiLabelMap.EcommerceStep} 3: ${uiLabelMap.PageTitleShippingOptions}</h3>
          <div id="shippingOptionSummaryPanel" class="screenlet-body" style="display: none;">
            <a href="javascript:void(0);" id="openShippingOptionPanel" class="button">${uiLabelMap.EcommerceClickHereToEdit}</a>
            <div class="completed" id="shippingOptionCompleted">
              <ul>
                <li>${uiLabelMap.CommonMethod}</li>
                <li id="selectedShipmentOption"></li>
              </ul>
            </div>
          </div>

<#-- ============================================================= -->
          <div id="editShippingOptionPanel" class="screenlet-body" style="display: none;">
            <form id="shippingOptionForm" action="<@ofbizUrl></@ofbizUrl>" method="post">
              <fieldset>
                  <div id="shippingOptionFormServerError" class="errorMessage"></div>
                  <div>
                      <label for="shipMethod">${uiLabelMap.OrderSelectShippingMethod}*
                          <span id="advice-required-shipping_method" class="custom-advice" style="display:none"> (${uiLabelMap.CommonRequired})</span>
                      </label>
                      <select id="shipMethod" name="shipMethod" class="required">
                          <option value=""></option>
                      </select>
                  </div>
              </fieldset>
              <fieldset>
                <a href="javascript:void(0);" class="button" id="saveShippingMethod">${uiLabelMap.EcommerceContinueToStep} 4</a>
                <a style="display:none" class="button" href="javascript:void(0);" id="processingBilling">${uiLabelMap.EcommercePleaseWait}....</a>
              </fieldset>
            </form>
          </div>
        </div>

<#-- ========================================================================================================================== -->
        <div id="billingPanel" class="screenlet">
          <h3>${uiLabelMap.EcommerceStep} 4: ${uiLabelMap.AccountingBilling}</h3>
          <div id="billingSummaryPanel" class="screenlet-body" style="display: none;">
            <a href="javascript:void(0);" id="openBillingPanel" class="button">${uiLabelMap.EcommerceClickHereToEdit}</a>
            <div class="completed" id="billingCompleted">
              <ul>
                <li>
                  <h4>${uiLabelMap.OrderBillUpTo}</h4>
                  <ul>
                    <li id="completedBillToAttn"></li>
                    <li id="completedBillToPhoneNumber"></li>
                    <li id="paymentMethod"></li>
                    <li id="completedCCNumber"></li>
                    <li id="completedExpiryDate"></li>
                  </ul>
                </li>
                <li>
                  <h4>${uiLabelMap.EcommerceLocation}</h4>
                  <ul>
                    <li id="completedBillToAddress1"></li>
                    <li id="completedBillToAddress2"></li>
                    <li id="completedBillToGeo"></li>
                  </ul>
                </li>
              </ul>
            </div>
          </div>

<#-- ============================================================= -->

          <div id="editBillingPanel" class="screenlet-body" style="display: none;">
            <form id="billingForm" class="theform" action="<@ofbizUrl></@ofbizUrl>" method="post">
              <fieldset class="col">
                  <input type="hidden" id ="billToContactMechId" name="billToContactMechId" value="${billToContactMechId?if_exists}" />
                  <input type="hidden" id="shipToContactMechIdInBillingForm" name="shipToContactMechId" value="${shipToContactMechId?if_exists}" />
                  <input type="hidden" id="paymentMethodId" name="paymentMethodId" value="${paymentMethodId?if_exists}" />
                  <input type="hidden" id="paymentMethodTypeId" name="paymentMethodTypeId" value="${paymentMethodTypeId?default("CREDIT_CARD")}" />
                  <input type="hidden" id="billToPartyId" name="partyId" value="${parameters.partyId?if_exists}" />
                  <input type="hidden" name="expireDate" value="${expireDate?if_exists}" />
                  <input type="hidden" id="billToPhoneContactMechId" name="billToPhoneContactMechId" value="${(billToTelecomNumber.contactMechId)?if_exists}" />
                  <input type="hidden" name="billToName" value="${billToName?if_exists}" />
                  <input type="hidden" name="billToAttnName" value="${billToAttnName?if_exists}" />
                  <#if userLogin?exists>
                    <input type="hidden" name="keepAddressBook" value="Y" />
                    <input type="hidden" name="setDefaultBilling" value="Y" />
                    <#assign productStoreId = Static["org.ofbiz.product.store.ProductStoreWorker"].getProductStoreId(request) />
                    <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}" />
                  <#else>
                    <input type="hidden" name="keepAddressBook" value="N" />
                  </#if>
                  <div id="billingFormServerError" class="errorMessage"></div>
                        <div>
                            <span>
                                <label for="firstNameOnCard">${uiLabelMap.PartyFirstName}*
                                    <span id="advice-required-firstNameOnCard" style="display: none;" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input id="firstNameOnCard" name="firstNameOnCard" class="required" type="text" value="${firstNameOnCard?if_exists}" />
                            </span>
                            <span>
                                <label for="lastNameOnCard">${uiLabelMap.PartyLastName}*
                                    <span id="advice-required-lastNameOnCard" style="display: none;" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input id="lastNameOnCard" name="lastNameOnCard" class="required" type="text" value="${lastNameOnCard?if_exists}" />
                            </span>
                        </div>
                        <div>  
                          <#if billToTelecomNumber?has_content>
                            <span>
                                <label for="billToCountryCode">${uiLabelMap.CommonCountry}*
                                    <span id="advice-required-billToCountryCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToCountryCode" class="required" id="billToCountryCode" value="${billToTelecomNumber.countryCode?if_exists}" size="5" maxlength="10" /> -
                            </span>
                            <span>
                                <label for="billToAreaCode">${uiLabelMap.PartyAreaCode}*
                                    <span id="advice-required-billToAreaCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToAreaCode" class="required" id="billToAreaCode" value="${billToTelecomNumber.areaCode?if_exists}" size="5" maxlength="10" /> -
                            </span>
                            <span>
                                <label for="billToContactNumber">${uiLabelMap.PartyContactNumber}*
                                    <span id="advice-required-billToContactNumber" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToContactNumber" class="required" id="billToContactNumber" value="${billToTelecomNumber.contactNumber?if_exists}" size="10" maxlength="15" /> -
                            </span>
                            <span>
                                <label for="billToExtension">${uiLabelMap.PartyExtension}</label>
                                <input type="text" name="billToExtension" id="billToExtension" value="${billToExtension?if_exists}" size="5" maxlength="10" />
                            </span>
                          <#else>
                            <span>
                                <label for="billToCountryCode">${uiLabelMap.CommonCountry}*
                                    <span id="advice-required-billToCountryCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToCountryCode" class="required" id="billToCountryCode" value="${parameters.billToCountryCode?if_exists}" size="5" maxlength="10" /> -
                            </span>
                            <span>
                                <label for="billToAreaCode">${uiLabelMap.PartyAreaCode}*
                                    <span id="advice-required-billToAreaCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToAreaCode" class="required" id="billToAreaCode" value="${parameters.billToAreaCode?if_exists}" size="5" maxlength="10" /> -
                            </span>
                            <span>
                                <label for="billToContactNumber">${uiLabelMap.PartyContactNumber}*
                                    <span id="advice-required-billToContactNumber" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input type="text" name="billToContactNumber" class="required" id="billToContactNumber" value="${parameters.billToContactNumber?if_exists}" size="10" maxlength="15" /> -
                            </span>
                            <span>
                                <label for="billToExtension">${uiLabelMap.PartyExtension}</label>
                                <input type="text" name="billToExtension" id="billToExtension" value="${parameters.billToExtension?if_exists}" size="5" maxlength="10" />
                            </span>
                          </#if>
                        </div>
                        <div>
                          <span>
                                <label for="cardType">${uiLabelMap.AccountingCardType}*<span id="advice-required-cardType" style="display: none;" class="errorMessage"> (${uiLabelMap.CommonRequired})</span></label>
                                <select name="cardType" id="cardType">
                                  <#if cardType?has_content>
                                    <option label="${cardType?if_exists}" value="${cardType?if_exists}">${cardType?if_exists}</option>
                                  </#if>
                                  ${screens.render("component://common/widget/CommonScreens.xml#cctypes")}
                                </select>
                          </span>
                        </div>
                        <div>
                            <span>
                                <label for="cardNumber">${uiLabelMap.AccountingCardNumber}*
                                    <span id="advice-required-cardNumber" style="display: none;" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                                </label>
                                <input id="cardNumber" name="cardNumber" class="required" type="text" value="${cardNumber?if_exists}" size="30" maxlength="16" />
                            </span>
                            <span>
                                <label for="billToCardSecurityCode">CVV2</label>
                                <input id="billToCardSecurityCode" name="billToCardSecurityCode" size="4" type="text" maxlength="4" value="" />
                            </span>
                        </div>
                        <div>
                          <span>
                            <label for="expMonth">${uiLabelMap.CommonMonth}:*
                                <span id="advice-required-expMonth" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <select id="expMonth" name="expMonth" class="required">
                              <#if expMonth?has_content>
                                <option label="${expMonth?if_exists}" value="${expMonth?if_exists}">${expMonth?if_exists}</option>
                              </#if>
                              ${screens.render("component://common/widget/CommonScreens.xml#ccmonths")}
                            </select>
                          </span>
                          <span>
                            <label for="expYear">${uiLabelMap.CommonYear}:*
                                <span id="advice-required-expYear" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                            </label>
                            <select id="expYear" name="expYear" class="required">
                              <#if expYear?has_content>
                                <option value="${expYear?if_exists}">${expYear?if_exists}</option>
                              </#if>
                              ${screens.render("component://common/widget/CommonScreens.xml#ccyears")}
                            </select>
                          </span>
                        </div>
                    </fieldset>
                    <fieldset class="col">
                        <div>
                                <input class="checkbox" id="useShippingAddressForBilling" name="useShippingAddressForBilling" type="checkbox" value="Y" <#if useShippingAddressForBilling?has_content && useShippingAddressForBilling?default("")=="Y">checked="checked"</#if> /><label for="useShippingAddressForBilling">${uiLabelMap.FacilityBillingAddressSameShipping}</label>
                        </div>
                        <div id="billingAddress" <#if useShippingAddressForBilling?has_content && useShippingAddressForBilling?default("")=="Y">style="display:none"</#if>>
                          <div>
                              <label for="billToAddress1">${uiLabelMap.PartyAddressLine1}*
                                <span id="advice-required-billToAddress1" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                              </label>
                              <input id="billToAddress1" name="billToAddress1" class="required" size="30" type="text" value="${billToAddress1?if_exists}" />
                          </div>
                          <div>
                              <label for="billToAddress2">${uiLabelMap.PartyAddressLine2}</label>
                              <input id="billToAddress2" name="billToAddress2" type="text" value="${billToAddress2?if_exists}" size="30" />
                          </div>
                          <div>
                              <label for="billToCity">${uiLabelMap.CommonCity}*
                                <span id="advice-required-billToCity" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                              </label>
                              <input id="billToCity" name="billToCity" class="required" type="text" value="${billToCity?if_exists}" />
                          </div>
                          <div>
                              <label for="billToPostalCode">${uiLabelMap.PartyZipCode}*
                                <span id="advice-required-billToPostalCode" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                              </label>
                              <input id="billToPostalCode" name="billToPostalCode" class="required" type="text" value="${billToPostalCode?if_exists}" size="12" maxlength="10" />
                          </div>
                          <div>
                              <label for="billToCountryGeoId">${uiLabelMap.CommonCountry}*
                                <span id="advice-required-billToCountryGeoId" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                              </label>
                              <select name="billToCountryGeoId" id="billToCountryGeoId">
                                <#if billToCountryGeoId?exists>
                                  <option value='${billToCountryGeoId?if_exists}'>${billToCountryProvinceGeo?default(billToCountryGeoId?if_exists)}</option>
                                </#if>
                                ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                              </select>
                          </div>
                          <div>
                              <label for="billToStateProvinceGeoId">${uiLabelMap.CommonState}*
                                <span id="advice-required-billToStateProvinceGeoId" style="display:none" class="errorMessage"> (${uiLabelMap.CommonRequired})</span>
                              </label>
                              <select id="billToStateProvinceGeoId" name="billToStateProvinceGeoId">
                                <#if billToStateProvinceGeoId?has_content>
                                  <option value='${billToStateProvinceGeoId?if_exists}'>${billToStateProvinceGeo?default(billToStateProvinceGeoId?if_exists)}</option>
                                <#else>
                                  <option value="_NA_">${uiLabelMap.PartyNoState}</option>
                                </#if>
                              </select>
                          </div>
                        </div>
                    </fieldset>
                    <br style="clear:both;"/>
                    <fieldset>
                      <a href="javascript:void(0);" class="button" id="savePaymentAndBillingContact">${uiLabelMap.EcommerceContinueToStep} 5</a>
                      <a href="javascript:void(0);" class="button" style="display: none;" id="processingOrderSubmitPanel">${uiLabelMap.EcommercePleaseWait}....</a>
                    </fieldset>
            </form>
          </div>
        </div>

<#-- ========================================================================================================================== -->
        <div class="screenlet">
          <h3>${uiLabelMap.EcommerceStep} 5: ${uiLabelMap.OrderSubmitOrder}</h3>
          <div id="orderSubmitPanel" style="display: none;">
            <form id="orderSubmitForm" action="<@ofbizUrl>onePageProcessOrder</@ofbizUrl>" method="post">
                <fieldset>
                    <input type="button" id="processOrderButton" name="processOrderButton" value="${uiLabelMap.OrderSubmitOrder}" />
                    <input type="button" style="display: none;" id="processingOrderButton" name="processingOrderButton" value="${uiLabelMap.OrderSubmittingOrder}" />
                </fieldset>
            </form>
          </div>
        </div>
      </div>
    </#if>

<#-- ========================================================================================================================== -->
    <div id="emptyCartCheckoutPanel" <#if shoppingCart?has_content && shoppingCart.size() &gt; 0> style="display: none;"</#if>>
        <h3>${uiLabelMap.EcommerceStep} 1: ${uiLabelMap.PageTitleShoppingCart}</h3>
        <span>You currently have no items in your cart. Click <a href="<@ofbizUrl>main</@ofbizUrl>">here</a> to view our products.</span>
        <h3>${uiLabelMap.EcommerceStep} 2: ${uiLabelMap.FacilityShipping}</h3>
        <h3>${uiLabelMap.EcommerceStep} 3: ${uiLabelMap.PageTitleShippingOptions}</h3>
        <h3>${uiLabelMap.EcommerceStep} 4: ${uiLabelMap.AccountingBilling}</h3>
        <h3>${uiLabelMap.EcommerceStep} 5: ${uiLabelMap.OrderSubmitOrder}</h3>
    </div>
</div>
