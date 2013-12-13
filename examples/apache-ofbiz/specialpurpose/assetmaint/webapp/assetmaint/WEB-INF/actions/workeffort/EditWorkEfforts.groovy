/*
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
*/

import java.util.*;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;

maintHistSeqId = context.maintHistSeqId;
fixedAssetId = context.fixedAssetId;
workEffortId = context.workEffortId;

if (!maintHistSeqId) {
    maintHistSeqId = parameters.maintHistSeqId;
}
if (!fixedAssetId) {
    fixedAssetId = parameters.fixedAssetId;
}
if (!workEffortId) {
    workEffortId = parameters.workEffortId;
}

fixedAssetMaint = null;
workEffort = null;
fixedAsset = null;
rootWorkEffortId = null;

if (workEffortId) {
    workEffort = delegator.findOne("WorkEffort", [workEffortId : workEffortId], false);
    if (workEffort) {
        if (!fixedAssetId) {
            fixedAssetId = workEffort.fixedAssetId;
        }
        // If this is a child workeffort, locate the "root" workeffort
        parentWorkEffort = EntityUtil.getFirst(delegator.findList("WorkEffortAssoc", EntityCondition.makeCondition([workEffortIdTo : workEffortId]), null, null, null, false));
        while (parentWorkEffort) {
            rootWorkEffortId = parentWorkEffort.workEffortIdFrom;
            parentWorkEffort = EntityUtil.getFirst(delegator.findList("WorkEffortAssoc", EntityCondition.makeCondition([workEffortIdTo : rootWorkEffortId]), null, null, null, false));
        }
    }
}

if (!rootWorkEffortId) {
    rootWorkEffortId = workEffortId;
}

if (rootWorkEffortId) {
    fixedAssetMaint = EntityUtil.getFirst(delegator.findList("FixedAssetMaint", EntityCondition.makeCondition([scheduleWorkEffortId : rootWorkEffortId]), null, null, null, false));
    if (fixedAssetMaint) {
        maintHistSeqId = fixedAssetMaint.maintHistSeqId;
        if (!fixedAssetId) {
            fixedAssetId = fixedAssetMaint.fixedAssetId;
        }
    }
}

if (fixedAssetId) {
    fixedAsset = delegator.findOne("FixedAsset", [fixedAssetId : fixedAssetId], false);
}

context.fixedAssetMaint = fixedAssetMaint;
context.workEffort = workEffort;
context.fixedAsset = fixedAsset;
context.maintHistSeqId = maintHistSeqId;
context.fixedAssetId = fixedAssetId;
context.workEffortId = workEffortId;
