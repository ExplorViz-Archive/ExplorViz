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
        <#-- Thread content id equals contentId if contentAssocTypeId equals PUBLISH_LINK, else threadContentId = ownerContentId -->
        <#assign threadContentId = forumMessage.contentId/>
        <#if forumMessage.caContentAssocTypeId == "RESPONSE">
            <#assign threadContentId = forumMessage.ownerContentId?if_exists/>
        </#if>
        <#if rsp?exists>
            <#assign contentId = rsp.contentId>
        <#else>
            <#assign contentId = forumMessage.contentId>
        </#if>
            <div class="tableheadtext">
                ${uiLabelMap.CommonTitle}: ${forumMessage.contentName?if_exists} ${uiLabelMap.CommonBy}:${forumMessage.createdByUserLogin?if_exists} ${uiLabelMap.CommonAt}: ${forumMessage.createdDate?if_exists.toString().substring(0,16)}
                <a href="ViewForumMessage?forumId=${parameters.forumId}&amp;responseContentId=${forumMessage.contentId}&amp;threadContentId=${threadContentId?if_exists}" class="buttontext">${uiLabelMap.CommonView}</a>
            </div>
            <div class="tableheadtext">
            ${forumMessage.description?if_exists}
            </div>
            
        <#--
          <#assign result = dispatcher.runSync("getPublicForumMessage", Static["org.ofbiz.base.util.UtilMisc"].toMap("contentId", contentId, "userLogin", userLogin))/>
          <#if (result.resultData)?exists>
            <div class="tableheadtext">
                ${uiLabelMap.CommonTitle}: ${result.resultData.content.description?if_exists} ${uiLabelMap.CommonBy}:${result.resultData.content.createdByUserLogin} ${uiLabelMap.CommonAt}: ${result.resultData.content.createdDate.toString().substring(0,16)}
                <a href="addForumMessage?forumId=${parameters.forumId}&amp;forumMessageIdTo=${result.resultData.content.contentId}&amp;threadView=${parameters.threadView?if_exists}" class="buttontext">${uiLabelMap.PartyReply}</a>
            </div>
            <div class="tabletext">
                <#if result.resultData.electronicText?exists>
                    ${result.resultData.electronicText.textData}
                </#if>
            </div>
            <hr />
          <#else>
              <div class="tabletext"> ${result.errorMessage?if_exists}</div>
        </#if>
        -->
