/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.*;
import java.util.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.webapp.website.WebSiteWorker;
import org.ofbiz.accounting.payment.PaymentWorker;

/*publicEmailContactLists = delegator.findByAnd("ContactList", [isPublic : "Y", contactMechTypeId : "EMAIL_ADDRESS"], ["contactListName"]);
context.publicEmailContactLists = publicEmailContactLists;*/

webSiteId = WebSiteWorker.getWebSiteId(request);
exprList = [];
exprListThruDate = [];
exprList.add(EntityCondition.makeCondition("webSiteId", EntityOperator.EQUALS, webSiteId));
exprListThruDate.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
exprListThruDate.add(EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
orCond = EntityCondition.makeCondition(exprListThruDate, EntityOperator.OR);
exprList.add(orCond);
topCond = EntityCondition.makeCondition(exprList, EntityOperator.AND);
webSiteContactList = delegator.findList("WebSiteContactList", topCond, null, null, null, false);

publicEmailContactLists = [];
webSiteContactList.each { webSiteContactList ->
    contactList = webSiteContactList.getRelatedOne("ContactList");
    contactListType = contactList.getRelatedOne("ContactListType");
    temp = [:];
    temp.contactList = contactList;
    temp.contactListType = contactListType;
    publicEmailContactLists.add(temp);
}
context.publicEmailContactLists = publicEmailContactLists;

if (userLogin) {
    partyAndContactMechList = delegator.findByAnd("PartyAndContactMech", [partyId : partyId, contactMechTypeId : "EMAIL_ADDRESS"], ["-fromDate"]);
    partyAndContactMechList = EntityUtil.filterByDate(partyAndContactMechList);
    context.partyAndContactMechList = partyAndContactMechList;
}


