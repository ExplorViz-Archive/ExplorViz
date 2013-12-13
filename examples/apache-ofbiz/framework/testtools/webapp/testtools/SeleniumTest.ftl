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

<#assign  libcheck  =  "">
<#assign  urlcheck  =  "">
<#assign msg = request.getAttribute("MSGMAP")?if_exists>
<#if msg?has_content>
    <#if msg.LIBFLAG?exists><#assign  libcheck  =  msg.LIBFLAG?if_exists?string></#if>
    <#if msg.URLFLAG?exists><#assign  urlcheck  =  msg.URLFLAG?if_exists?string></#if>
</#if>
<a id="verifly" href="<@ofbizUrl>verify</@ofbizUrl>" class="buttontext">Verify</a>
<table class="basic-table hover-bar" cellspacing="0">
        <tr class="header-row" >
          <td>Description</td>
          <td>Status</td>
        </tr>
        <tr>
          <td>- selenium_server.jar </td>
          <td>
                <#if libcheck == "true">
                    Success
                <#elseif libcheck == "noInternet">
                    Download and install selenium-server.jar in framework/testtools/lib (see in VerifySeleniumSetups.java)
                <#else>
                    <div style="color:red;" id="message">please click download this lib for run the selenium application!</div>
                    <div id="loadpercent"><a id="download" href="javascript:clickDownLoad('<@ofbizUrl>downloadSeleniumlib</@ofbizUrl>');" class="buttontext">download</a></div>
                    <div id="progress_bar"  style="display:none"></div>
                    <div id="filesize"  style="display:none"></div>
                </#if>
          </td>
        </tr>
        <tr valign="middle" class="alternate-row">
          <td>- Changes ofbiz to use HTTP as the default at file (framework/webapp/config/url.properties)</td>
          <td>
                <#if  urlcheck == "true">
                    Success
                <#else>
                    <div style="color:red;">Not success!</div>
                </#if>
          </td>
        </tr>
        <tr>
          <td>
            <ol>
            <li>Edit 'framework/testtools/config/seleniumXml.properties' and  add your firefox path, replace the example in the file</li>
            <li>Then save and restart ofbiz.</li>
            <li>Then open a new terminal to start the seleniumserver. (cd framework/testtools;./runSeleniumServer.sh)</li>
            <li>Then run the test below.</li>
            </ol>
          </td>
          <td>
          </td>
        </tr>
      </table>
<script type="text/javascript">
    var progressBar;
    // click start load call servlet & new progressBar
    function clickDownLoad(url){
         startDownLoad(url);
         document.getElementById('progress_bar').style.display = "";
         document.getElementById('filesize').style.display = "";
         progressBar = jQuery("#progress_bar").progressbar({value: 0});
    }

    function startDownLoad(url){
        jQuery.ajax({
            url: url,
            type: "POST",
            beforeSend: getProgressDownloadStatus,
            complete: function(data) {document.getElementById('download').innerHTML = '${uiLabelMap.CommonLoading}'}
        });
    }

    //function PeriodicalExecuter check download status
    function getProgressDownloadStatus(){
        jQuery.fjTimer({
            interval: 1000,
            repeat: true,
            tick: function(counter, timerId) {
                jQuery.ajax({
                    url: "<@ofbizUrl>progressDownloadStatus</@ofbizUrl>",
                    type: "GET",
                    succuess: function(data) {
                        if( data != null ){
                               if(data.contentLength != null && data.loadPercent != null){
                                   var loadPercent = data.loadPercent;
                                   document.getElementById('loadpercent').innerHTML = ''+loadPercent+'%';
                                   var contentLength  = data.contentLength;
                                   progressBar.progressbar("option", "value", loadPercent);
                                   if(loadPercent > 99){
                                        document.getElementById('download').innerHTML = '${uiLabelMap.CommonDownloaded}';
                                        timerId.stop();
                                   }
                               }
                        }
                    }
                });
            }
        });
    }
</script>
