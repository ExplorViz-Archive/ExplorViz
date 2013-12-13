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

import java.util.*;
import java.lang.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.util.*;

productId = parameters.productId;
loginPartyId = userLogin.partyId;
communicationEventId = parameters.communicationEventId;
now = UtilDateTime.nowTimestamp();
try{
    if (UtilValidate.isNotEmpty(loginPartyId)) {
        if (UtilValidate.isNotEmpty(productId)) {
        context.product = delegator.findByPrimaryKey("Product",["productId" : productId]);
        }
        communicationEvent = delegator.findByPrimaryKey("CommunicationEvent",["communicationEventId" : communicationEventId]);
        communicationEvent.communicationEventTypeId = "EMAIL_COMMUNICATION";
        communicationEvent.contactMechTypeId = "EMAIL_ADDRESS";
        communicationEvent.datetimeStarted = now;
        checkOwner = delegator.findByAnd("ProductRole",["productId" : productId,"partyId" : loginPartyId,"roleTypeId" : "PRODUCT_OWNER"]);
        if (checkOwner) {
            /* for product owner to our company */
            
            // for owner
            productRole = delegator.findByAnd("ProductRole",["productId" : productId,"roleTypeId" : "PRODUCT_OWNER"]);
            context.productOwnerId = productRole[0].partyId
            parentCom = delegator.findByPrimaryKey("CommunicationEvent",["communicationEventId" : communicationEventId]);
            if (parentCom) {
                context.partyIdFrom = productRole[0].partyId;
            } else {
                context.partyIdFrom = parentCom.partyIdTo;
            }
            resultsIdFrom = dispatcher.runSync("getPartyEmail", ["partyId" : productRole[0].partyId, "userLogin" : userLogin]);
            if (resultsIdFrom.contactMechId != null) {
                context.contactMechIdFrom = resultsIdFrom.contactMechId;
                communicationEvent.contactMechIdFrom = resultsIdFrom.contactMechId;
            }
            // for team
            defaultPartyIdTo = organizationPartyId;
            resultsIdTo = dispatcher.runSync("getPartyEmail", ["partyId" : defaultPartyIdTo,"contactMechPurposeTypeId" :"SUPPORT_EMAIL", "userLogin" : userLogin]);
            if (resultsIdTo.contactMechId != null) {
                context.contactMechIdTo = resultsIdTo.contactMechId;
                communicationEvent.contactMechIdTo = resultsIdTo.contactMechId;
            }
            context.partyIdTo = defaultPartyIdTo;
            communicationEvent.store();
            context.communicationEvent = communicationEvent;
        } else {
            /* from company to owner */
            
            // for team
            defaultPartyIdFrom = organizationPartyId;
            context.partyIdFrom = defaultPartyIdFrom;
            resultsIdFrom = dispatcher.runSync("getPartyEmail", ["partyId" : defaultPartyIdFrom,"contactMechPurposeTypeId" :"SUPPORT_EMAIL", "userLogin" : userLogin]);
            if (resultsIdFrom.contactMechId != null) {
                context.contactMechIdFrom = resultsIdFrom.contactMechId;
                communicationEvent.contactMechIdFrom = resultsIdFrom.contactMechId;
            }
            // for owner
            productRole = delegator.findByAnd("ProductRole",["productId" : productId,"roleTypeId" : "PRODUCT_OWNER"]);
            context.productOwnerId = productRole[0].partyId;
            parentCom = delegator.findByPrimaryKey("CommunicationEvent",["communicationEventId" : communicationEventId]);
            if(parentCom){
                context.partyIdTo = productRole[0].partyId;
            } else {
                 context.partyIdTo = parentCom.partyIdFrom;
            }
           resultsIdTo = dispatcher.runSync("getPartyEmail", ["partyId" : productRole[0].partyId, "userLogin" : userLogin]);
           if (resultsIdTo.contactMechId != null) {
              context.contactMechIdTo = resultsIdTo.contactMechId;
              communicationEvent.contactMechIdTo = resultsIdTo.contactMechId;
           }
           communicationEvent.store();
           context.communicationEvent = communicationEvent;
       }
    }
} catch (exeption) {
    Debug.logInfo("catch exeption ================" + exeption,"");
} catch (GenericEntityException e) {
    Debug.logInfo("catch GenericEntityException ================" + e.getMessage(),"");
}
