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

<h2>XPDL Repository</h2>
<#if packages?has_content>
  <div>&nbsp;</div>
  <div>XPDL packages loaded into the repository.</div>
  <table cellpadding="2" cellspacing="0" border="1">
    <tr>
      <td><div>ID</div></td>
      <td><div>Version</div></td>
      <td><div>Is Open</div></td>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <#list packages as package>
      <#if (!package?starts_with("CVS"))>
        <#assign packageId = repMgr.getPackageId(package)>
        <#assign open = pkgMgr.isPackageOpened(packageId)?default(false)>
        <#if (open)>
            <#assign version = pkgMgr.getCurrentPackageVersion(packageId)?if_exists>
        <#else>
            <#assign version = "<closed>">
        </#if>

        <tr>
          <td><div>${packageId?default("??")}</div>
          <td><div>${version?default("??")}</div></td>
          <td align="center"><div><#if open>Y<#else>N</#if></div>
          <td align="center"><a href="<@ofbizUrl>repository?delete=${package}</@ofbizUrl>" class="buttontext">Remove</a>
          <td align="center"><a href="<@ofbizUrl>repository?<#if open>close=${packageId}&amp;version=${version}<#else>open=${package}</#if></@ofbizUrl>" class="buttontext"><#if open>Close<#else>Open</#if></a>
        </tr>
      </#if>
    </#list>
  </table>
<#else>
  <div>Repository is empty.</div>
</#if>

<br />
<h2>Upload XPDL</h2>
<div>&nbsp;</div>
<form method="post" enctype="multipart/form-data" action="<@ofbizUrl>repository?upload=xpdl</@ofbizUrl>" name="xpdlUploadForm">
  <input type="file" size="50" name="fname" />
  <div><hr/></div>
  <input type="submit" class="smallSubmit" value="Upload" />
</form>
