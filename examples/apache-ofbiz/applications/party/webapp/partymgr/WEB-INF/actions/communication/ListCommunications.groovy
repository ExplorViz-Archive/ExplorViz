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

import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.condition.EntityCondition;

import javolution.util.FastList;

partyId = parameters.partyId;
context.partyId = partyId;

party = delegator.findByPrimaryKey("Party", [partyId : partyId]);
context.party = party;

// get the sort field
sortField = parameters.sort ?: "entryDate";
context.previousSort = sortField;

// previous sort field
previousSort = parameters.previousSort;
if (previousSort?.equals(sortField)) {
    sortField = "-" + sortField;
}

List eventExprs = FastList.newInstance();
expr = EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId);
eventExprs.add(expr);
expr = EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, "partyId");
eventExprs.add(expr);
ecl = EntityCondition.makeCondition(eventExprs, EntityOperator.OR);
events = delegator.findList("CommunicationEvent", ecl, null, [sortField], null, false);

context.eventList = events;
context.eventListSize = events.size();
context.highIndex = events.size();
context.viewSize = events.size();
context.lowIndex = 1;
context.viewIndex = 1;
