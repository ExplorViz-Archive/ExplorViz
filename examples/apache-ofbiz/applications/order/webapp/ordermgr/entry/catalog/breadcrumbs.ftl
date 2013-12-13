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
<div class="breadcrumbs">
<#assign isDefaultTheme = !layoutSettings.VT_FTR_TMPLT_LOC?contains("multiflex")>        
<#if isDefaultTheme>
  <a href="<@ofbizUrl>main</@ofbizUrl>" class="linktext">${uiLabelMap.CommonMain}</a> &gt;
<#else>
  <ul>
    <li>
      <a href="<@ofbizUrl>main</@ofbizUrl>" class="linktext">${uiLabelMap.CommonMain}</a>
    </li>
</#if>    
    <#-- Show the category branch -->
    <#assign crumbs = Static["org.ofbiz.product.category.CategoryWorker"].getTrail(request)/>
    <#list crumbs as crumb>
         <#if catContentWrappers?exists && catContentWrappers[crumb]?exists>
            <#if !isDefaultTheme>         
              <li>
                 <a href="<@ofbizCatalogUrl currentCategoryId=crumb previousCategoryId=previousCategoryId!""/>" class="<#if crumb_has_next>linktext<#else>buttontextdisabled</#if>">
                   <#if catContentWrappers[crumb].get("CATEGORY_NAME")?exists>
                     ${catContentWrappers[crumb].get("CATEGORY_NAME")}
                   <#elseif catContentWrappers[crumb].get("DESCRIPTION")?exists>
                     ${catContentWrappers[crumb].get("DESCRIPTION")}
                   <#else>
                     ${crumb}
                   </#if>
                 </a>
              </li>
            <#else>  
               <a href="<@ofbizCatalogUrl currentCategoryId=crumb previousCategoryId=previousCategoryId!""/>" class="<#if crumb_has_next>linktext<#else>buttontextdisabled</#if>">
                 <#if catContentWrappers[crumb].get("CATEGORY_NAME")?exists>
                   ${catContentWrappers[crumb].get("CATEGORY_NAME")}
                 <#elseif catContentWrappers[crumb].get("DESCRIPTION")?exists>
                   ${catContentWrappers[crumb].get("DESCRIPTION")}
                 <#else>
                   ${crumb}
                 </#if>
               </a>
               <#if crumb_has_next> &gt;</#if>               
            </#if>  
            <#assign previousCategoryId = crumb />
         </#if>
    </#list>    
    <#-- Show the product, if there is one -->
    <#if productContentWrapper?exists>
      <#if isDefaultTheme>        
         &nbsp;&gt; ${productContentWrapper.get("PRODUCT_NAME")?if_exists}
      <#else>
          <li>${productContentWrapper.get("PRODUCT_NAME")?if_exists}</li>
        </ul>  
      </#if>
    </#if>
</div>
