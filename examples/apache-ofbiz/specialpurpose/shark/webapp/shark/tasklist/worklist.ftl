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

<h2>Work List</h2>
<#if assignments?has_content>
  <div>&nbsp;</div>

  <table cellpadding="2" cellspacing="0" border="1">
    <tr>
      <td><div>Resource</div></td>
      <td><div>Name</div></td>
      <td><div>Activity</div></td>
      <td><div>Accepted</div></td>
      <td><div>Priority</div></td>
      <td><div>State</div></td>
      <td><div>State Change</div></td>
      <td>&nbsp;</td>
    </tr>
    <#assign formNumber = 0>
    <#list assignments as assignm>
      <#assign formNumber = formNumber + 1>
      <#assign state = assignm.activity().state()?default("open.not_running.not_started")>
      <#if state != "open.not_running.not_started">
        <#assign time = assignm.activity().last_state_time().getTime()>
      <#else>
        <#assign time = 0>
      </#if>
      <tr>
        <form method="post" action="<@ofbizUrl>worklist</@ofbizUrl>" name="assignmentChange${formNumber}" style='margin: 0;'>
          <input type="hidden" name="process" value="${assignm.activity().container().key()}" />
          <input type="hidden" name="activity" value="${assignm.activity().key()}" />
          <input type="hidden" name="resource" value="${assignm.assignee().resource_key()}" />
          <input type="hidden" name="mode" value="accept" />

          <td><div>${assignm.assignee().resource_key()}</div></td>
          <td><div>${assignm.activity().name()}</div></td>
          <td><div>${assignm.activity().key()}</div></td>
          <td align="center">
            <input type="checkbox" name="accept" onclick="javascript:document.assignmentChange${formNumber}.submit();" value="Y" <#if assignm.get_accepted_status()>checked="checked"</#if> />
          </td>
          <td align="center"><div>${assignm.activity().priority()}</div></td>
          <td><div>${assignm.activity().state()}</div></td>
          <#if (time > 0)>
            <td><div>${Static["org.ofbiz.base.util.UtilDateTime"].getTimestamp(time)}</div></td>
          <#else>
            <td align="center"><div>N/A</div></td>
          </#if>
          <#if assignm.activity().state() == "open.running">
            <td align="center"><a href="<@ofbizUrl>worklist?mode=complete&amp;resource=${assignm.assignee().resource_key()}&amp;process=${assignm.activity().container().key()}&amp;activity=${assignm.activity().key()}</@ofbizUrl>" class="buttontext">Complete</a></td>
          <#else>
            <td>&nbsp;</td>
          </#if>
        </form>
      </tr>
    </#list>
  </table>
<#else>
  <div>No tasks available.</div>
</#if>
