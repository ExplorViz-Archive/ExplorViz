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

<#assign components = Static["org.ofbiz.base.component.ComponentConfig"].getAllComponents()?if_exists/>
<#if (requestParameters.certString?has_content)>
    <#assign cert = Static["org.ofbiz.base.util.KeyStoreUtil"].pemToCert(requestParameters.certString)/>
</#if>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <h3>${uiLabelMap.CertDetails}</h3>
  </div>
  <div class="screenlet-body">
    <#if (cert?has_content)>
        <span class="label">${uiLabelMap.CertType}</span>&nbsp;${cert.getType()} : ${cert.getSubjectX500Principal()}
        <span class="label">${uiLabelMap.CertName}</span>&nbsp;${cert.getSubjectX500Principal().getName()}
        <span class="label">${uiLabelMap.CertSerialNumber}</span>&nbsp;${cert.getSerialNumber().toString(16)}
    <#else>
        <h3>${uiLabelMap.CertInvalid}</h3>
    </#if>
  </div>
</div>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <h3>${uiLabelMap.CertSaveToKeyStore}</h3>
  </div>
  <div class="screenlet-body">
    <table cellspacing="0" class="basic-table">
      <tr class="header-row">
        <td>${uiLabelMap.CertComponent}</td>
        <td>${uiLabelMap.CertKeyStore}</td>
        <td>${uiLabelMap.CertImportIssuer}</td>
        <td>${uiLabelMap.CertKeyAlias}</td>
        <td>&nbsp;</td>
      </tr>
      <#list components as component>
        <#assign keystores = component.getKeystoreInfos()?if_exists/>
          <#list keystores as store>
            <#if (store.isTrustStore())>
              <tr>
                <form method="post" action="<@ofbizUrl>/importIssuerProvision</@ofbizUrl>">
                  <input type="hidden" name="componentName" value="${component.getComponentName()}"/>
                  <input type="hidden" name="keystoreName" value="${store.getName()}"/>
                  <input type="hidden" name="certString" value="${requestParameters.certString}"/>
                  <td>${component.getComponentName()}</td>
                  <td>${store.getName()}</td>
                  <td align="center"><input type="checkbox" name="importIssuer" value="Y"/>
                  <td><input type="text" name="alias" size="20"/>
                  <td align="right"><input type="submit" value="${uiLabelMap.CommonSave}"/>
                </form>
              </tr>
            </#if>
          </#list>
      </#list>
    </table>
  </div>
</div>