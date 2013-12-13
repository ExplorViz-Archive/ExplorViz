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
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.base.util.*;
import javolution.util.FastMap;
import javolution.util.FastList;


cond =
    EntityCondition.makeCondition(
            [EntityCondition.makeCondition ("workEffortTypeId", EntityOperator.EQUALS, "PROJECT"),
             EntityCondition.makeCondition ("partyId", EntityOperator.EQUALS, parameters.partyId)
            ],EntityOperator.AND);
allProjects = delegator.findList("WorkEffortAndPartyAssign", cond, (HashSet) ["workEffortId"], ["workEffortName"], null, true);

projects = [];
allProjects.each { project ->
    result = dispatcher.runSync("getProject", ["userLogin" : parameters.userLogin, "projectId" : project.workEffortId, partyId : parameters.partyId]);
    projects.add(result.projectInfo);
}
if (projects) {
    context.billingList = projects;
}


