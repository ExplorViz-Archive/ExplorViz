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
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>${title}</title>
        <link rel="stylesheet" href="${baseSecureUrl?if_exists}/images/maincss.css" type="text/css"/>
    </head>
    <body>
        <h2>${title}</h2>
        <p>Hello ${person.firstName?if_exists} ${person.lastName?if_exists},</p>
        <p>Your Customer Request ${custRequest.custRequestName?if_exists} [${custRequest.custRequestId}] has a note added to it:<br />
        ${StringUtil.wrapString(noteData.noteInfo?if_exists)}
        <br /><br />
        Please login to the url below and add another note as a reply if required.<br /><br />
        The status and used hours can always be checked <br />
        <a href="${StringUtil.wrapString(baseSecureUrl?if_exists)}/myportal/control/showPortletDecorator?portalPortletId=ViewCustRequest&amp;id=${custRequest.custRequestId}">here.....</a>
        <br /><br />
        Regards.<br /><br />
        Thank you for your business.
    </body>
</html>
