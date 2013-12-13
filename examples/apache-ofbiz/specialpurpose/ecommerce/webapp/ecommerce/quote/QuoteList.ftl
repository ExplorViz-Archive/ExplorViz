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
    <h3>${uiLabelMap.EcommerceQuoteHistory}</h3>
    <div class="screenlet-body">
        <table>
            <tr>
                <td width="10%">
                    <div class="tabletext"><span style="white-space: nowrap;">${uiLabelMap.OrderQuote} ${uiLabelMap.CommonNbr}</span></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="20%">
                    <div class="tabletext">${uiLabelMap.CommonName}</div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="40%">
                    <div class="tabletext">${uiLabelMap.CommonDescription}</div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="10%">
                    <div class="tabletext">${uiLabelMap.CommonStatus}</div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="20%">
                    <div class="tabletext">${uiLabelMap.OrderOrderQuoteIssueDate}</div>
                    <div class="tabletext">${uiLabelMap.CommonValidFromDate}</div>
                    <div class="tabletext">${uiLabelMap.CommonValidThruDate}</div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="10">&nbsp;</td>
            </tr>
            <#list quoteList as quote>
                <#assign status = quote.getRelatedOneCache("StatusItem")>
                
                <tr>
                    <td>
                        <div class="tabletext">${quote.quoteId}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${quote.quoteName?if_exists}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${quote.description?if_exists}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${status.get("description",locale)}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext"><span style="white-space: nowrap;">${quote.issueDate?if_exists}</span></div>
                        <div class="tabletext"><span style="white-space: nowrap;">${quote.validFromDate?if_exists}</span></div>
                        <div class="tabletext"><span style="white-space: nowrap;">${quote.validThruDate?if_exists}</span></div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td align="right">
                        <a href="<@ofbizUrl>ViewQuote?quoteId=${quote.quoteId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonView}</a>
                    </td>
                    <td width="10">&nbsp;</td>
                </tr>
            </#list>
            <#if !quoteList?has_content>
                <tr><td colspan="9"><h3>${uiLabelMap.OrderNoQuoteFound}</h3></td></tr>
            </#if>
        </table>
    </div>
</div>
