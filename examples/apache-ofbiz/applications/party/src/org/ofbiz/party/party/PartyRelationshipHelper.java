/*******************************************************************************
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
 *******************************************************************************/

package org.ofbiz.party.party;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

/**
 * PartyRelationshipHelper
 */
public class PartyRelationshipHelper {

    public static final String module = PartyRelationshipHelper.class.getName();

    /** Return A List of the active Party Relationships (ie with valid from and thru dates)
     *@param delegator needed Delegator
     *@param partyRelationshipValues Map containing the input parameters (primaries keys + partyRelationshipTypeId)
     *@return List of the active Party Relationships
     */
    public static List<GenericValue> getActivePartyRelationships(Delegator delegator, Map<String, ?> partyRelationshipValues) {
        String partyIdFrom = (String) partyRelationshipValues.get("partyIdFrom") ;
        String partyIdTo = (String) partyRelationshipValues.get("partyIdTo") ;
        String roleTypeIdFrom = (String) partyRelationshipValues.get("roleTypeIdFrom") ;
        String roleTypeIdTo = (String) partyRelationshipValues.get("roleTypeIdTo") ;
        String partyRelationshipTypeId = (String) partyRelationshipValues.get("partyRelationshipTypeId") ;
        Timestamp fromDate = UtilDateTime.nowTimestamp();

        List<EntityCondition> condList = FastList.newInstance();
        condList.add(EntityCondition.makeCondition("partyIdFrom", partyIdFrom));
        condList.add(EntityCondition.makeCondition("partyIdTo", partyIdTo));
        condList.add(EntityCondition.makeCondition("roleTypeIdFrom", roleTypeIdFrom));
        condList.add(EntityCondition.makeCondition("roleTypeIdTo", roleTypeIdTo));
        condList.add(EntityCondition.makeCondition("partyRelationshipTypeId", partyRelationshipTypeId));
        condList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, fromDate));
        EntityCondition thruCond = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("thruDate", null),
                EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, fromDate)),
                EntityOperator.OR);
        condList.add(thruCond);
        EntityCondition condition = EntityCondition.makeCondition(condList);

        List<GenericValue> partyRelationships = null;
        try {
            partyRelationships = delegator.findList("PartyRelationship", condition, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem finding PartyRelationships. ", module);
            return null;
        }
        if (UtilValidate.isNotEmpty(partyRelationships)) {
           return partyRelationships;
        } else {
            return null;
        }
    }
}
