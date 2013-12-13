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
        <div class="h3">${uiLabelMap.CommonDate}</div>
    </div>
    <div class="screenlet-body">
        <table cellspacing="0" class="basic-table">
            <tr>
                <td align="right" valign="top" width="25%" class="label">
                    &nbsp;${uiLabelMap.OrderRequestDate}
                </td>
                <td width="5%">&nbsp;</td>
                <td valign="top" width="70%">
                    ${(custRequest.custRequestDate.toString())?if_exists}
                </td>
            </tr>
            <tr><td colspan="7"><hr /></td></tr>
            <tr>
                <td align="right" valign="top" width="25%" class="label">
                    &nbsp;${uiLabelMap.OrderRequestCreatedDate}
                </td>
                <td width="5%">&nbsp;</td>
                <td valign="top" width="70%">
                    ${(custRequest.createdDate.toString())?if_exists}
                </td>
            </tr>
            <tr><td colspan="7"><hr /></td></tr>
            <tr>
                <td align="right" valign="top" width="25%" class="label">
                    &nbsp;${uiLabelMap.OrderRequestLastModifiedDate}
                </td>
                <td width="5%">&nbsp;</td>
                <td valign="top" width="70%">
                    ${(custRequest.lastModifiedDate.toString())?if_exists}
                </td>
            </tr>
        </table>
    </div>
</div>
