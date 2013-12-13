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

import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.base.util.*

module = "showvisits.groovy";

partyId = parameters.partyId;
context.partyId = partyId;

showAll = parameters.showAll ?:"false";
context.showAll = showAll;

sort = parameters.sort;
context.sort = sort;

visitListIt = null;
sortList = ["-fromDate"];
if (sort) sortList.add(0, sort);

boolean beganTransaction = false;
try {
    beganTransaction = TransactionUtil.begin();

    viewIndex = Integer.valueOf(parameters.VIEW_INDEX  ?: 1);
    viewSize = Integer.valueOf(parameters.VIEW_SIZE ?: UtilProperties.getPropertyValue("widget", "widget.form.defaultViewSize"));
    context.viewIndex = viewIndex;
    context.viewSize = viewSize;

    // get the indexes for the partial list
    lowIndex = (((viewIndex - 1) * viewSize) + 1);
    highIndex = viewIndex * viewSize;

    if (partyId) {
        visitListIt = delegator.find("Visit", EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId), null, null, sortList, new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true));
    } else if (showAll.equalsIgnoreCase("true")) {
        visitListIt = delegator.find("Visit", null, null, null, sortList, new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true));
    } else {
        // show active visits
        visitListIt = delegator.find("Visit", EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), null, null, sortList, new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true));
    }

    // get the partial list for this page
    visitList = visitListIt.getPartialList(lowIndex, viewSize);
    if (!visitList) {
        visitList = new ArrayList();
    }

    visitListSize = visitListIt.getResultsSizeAfterPartialList();
    if (highIndex > visitListSize) {
        highIndex = visitListSize;
    }
    context.visitSize = visitListSize;

    visitListIt.close();
} catch (Exception e) {
    String errMsg = "Failure in operation, rolling back transaction";
    Debug.logError(e, errMsg, module);
    try {
        // only rollback the transaction if we started one...
        TransactionUtil.rollback(beganTransaction, errMsg, e);
    } catch (Exception e2) {
        Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
    }
    // after rolling back, rethrow the exception
    throw e;
} finally {
    // only commit the transaction if we started one... this will throw an exception if it fails
    TransactionUtil.commit(beganTransaction);
}

context.visitList = visitList;
listSize = 0;
if (visitList) {
    listSize = lowIndex + visitList.size();
}

if (listSize < highIndex) {
    highIndex = listSize;
}
context.lowIndex = lowIndex;
context.highIndex = highIndex;
context.listSize = listSize;
