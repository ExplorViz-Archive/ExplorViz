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
      <ul>
        <li class="h3">&nbsp;${uiLabelMap.WorkEffortNotes}</li>
          <#--if project?has_content>
            <li><a href="<@ofbizUrl>newNotesForProject?workEffortId=${project.workEffortId?if_exists}&amp;showForm=Y</@ofbizUrl>">${uiLabelMap.ProjectMgrNotesCreateNew}</a></li>
          <#else>
            <li><a href="<@ofbizUrl>newNotesForTask?workEffortId=${task.workEffortId?if_exists}&amp;showForm=Y</@ofbizUrl>">${uiLabelMap.ProjectMgrNotesCreateNew}</a></li>
          </#if-->
      </ul>
      <br class="clear" />
    </div>
    <div class="screenlet-body">
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
            <#if workEffortNoteandDetails?has_content>
            <table width="100%" border="0" cellpadding="1">
              <#list workEffortNoteandDetails as note>
                <tr>
                  <td valign="top" width="35%">
                    <div>&nbsp;<b>${uiLabelMap.CommonBy}: </b>${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, note.noteParty, true)}</div>
                    <div>&nbsp;<b>${uiLabelMap.CommonAt}: </b>${Static["org.ofbiz.base.util.UtilDateTime"].timeStampToString(note.noteDateTime?if_exists,"dd-MM-yyyy HH:mm",Static["java.util.TimeZone"].getDefault(),context.get("locale"))}</div>
                  </td>
                  <td valign="top" width="50%">
                    <div>${note.noteInfo?if_exists}</div>
                  </td>
                  <td align="right" valign="top" width="15%">
                    <#if note.internalNote?if_exists == "N">
                        <div>${uiLabelMap.ProjectMgrPrintableNote}</div>
                          <#if project?has_content>
                            <a href="<@ofbizUrl>updateProjectNote?workEffortId=${project.workEffortId?if_exists}&amp;noteId=${note.noteId}&amp;internalNote=Y</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderNotesPrivate}</a>
                          <#else>
                            <a href="<@ofbizUrl>updateTaskNoteSummary?workEffortId=${task.workEffortId?if_exists}&amp;noteId=${note.noteId}&amp;internalNote=Y</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderNotesPrivate}</a>
                          </#if>
                    </#if>
                    <#if note.internalNote?if_exists == "Y">
                        <div>${uiLabelMap.OrderNotPrintableNote}</div>
                           <#if project?has_content>
                             <a href="<@ofbizUrl>updateProjectNote?workEffortId=${project.workEffortId?if_exists}&amp;noteId=${note.noteId}&amp;internalNote=N</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderNotesPublic}</a>
                          <#else>
                            <a href="<@ofbizUrl>updateTaskNoteSummary?workEffortId=${task.workEffortId?if_exists}&amp;noteId=${note.noteId}&amp;internalNote=N</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderNotesPublic}</a>
                          </#if>
                    </#if>
                  </td>
                </tr>
                <#if note_has_next>
                  <tr><td colspan="3"><hr/></td></tr>
                </#if>
              </#list>
            </table>
            <#else>
              <#if project?has_content>
                <div>&nbsp;${uiLabelMap.ProjectMgrProjectNoNotes}.</div>
              <#else>
                <div>&nbsp;${uiLabelMap.ProjectMgrTaskNoNotes}.</div>
              </#if>

            </#if>
          </td>
        </tr>
      </table>
      <#if parameters.showForm?exists>
        <div class="screenlet-title-bar">
          <ul>
          <li class="h3">&nbsp;${uiLabelMap.OrderAddNote}</li>
          </ul>
          <br class="clear" />
        </div>
        <div class="screenlet-body">
          <form name="createnoteform" method="post"
            <#if project?has_content> action="<@ofbizUrl>createNewNotesForProject</@ofbizUrl>"
            <#else> action="<@ofbizUrl>createNewNotesForTask</@ofbizUrl>"
            </#if>>
            <table width="90%" border="0" cellpadding="2" cellspacing="0">
              <tr>
                <#if project?has_content>
                  <td><input type="hidden" name="workEffortId" value="${project.workEffortId}" /></td>
                <#else>
                  <td><input type="hidden" name="workEffortId" value="${task.workEffortId}" /></td>
                </#if>
              </tr>
              <tr>
                <td width="26%" align="right"><div>${uiLabelMap.OrderNote}</div></td>
                <td width="54%">
                  <textarea name="noteInfo" rows="5" cols="70"></textarea>
                </td>
              </tr>
              <tr>
                <td/><td>${uiLabelMap.OrderInternalNote} :
                  <select name="internalNote" size="1"><option value=""></option><option value="Y" selected>${uiLabelMap.CommonYes}</option><option value="N">${uiLabelMap.CommonNo}</option></select></td>
              </tr>
              <tr>
                <td/><td><i>${uiLabelMap.OrderInternalNoteMessage}</i></td>
              </tr>
            </table>
            <#if project?has_content>
              &nbsp;<a href="javascript:document.createnoteform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>
            <#else>
              &nbsp;<a href="javascript:document.createnoteform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>
            </#if>
          </form>
        </div>
      </#if>
    </div>
  </div>

