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
import java.net.*;
import org.ofbiz.security.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.webapp.pseudotag.*;
import org.ofbiz.workflow.definition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.transaction.*;

messages = [];

xpdlLoc = parameters.XPDL_LOCATION;
xpdlIsUrl = parameters.XPDL_IS_URL != null;
xpdlImport = parameters.XPDL_IMPORT != null;

URL xpdlUrl = null;
try {
    xpdlUrl = xpdlIsUrl ? new URL(xpdlLoc) : UtilURL.fromFilename(xpdlLoc);
} catch (java.net.MalformedURLException e) {
    messages.add(e.getMessage());
    messages.add(e.toString());
    Debug.log(e);
}
if (!xpdlUrl) {
    messages.add("Could not find file/URL: " + xpdlLoc);
}

toBeStored = null;
try {
    if (xpdlUrl) {
        toBeStored = XpdlReader.readXpdl(xpdlUrl, delegator);
        context.toBeStored = toBeStored;
    }
} catch (Exception e) {
    messages.add(e.getMessage());
    messages.add(e.toString());
    Debug.log(e);
}

if (toBeStored && xpdlImport) {
    beganTransaction = false;
    try {
        beganTransaction = TransactionUtil.begin();
        delegator.storeAll(toBeStored);
        TransactionUtil.commit(beganTransaction);
        messages.add("Wrote/Updated " + toBeStored.size() + " toBeStored objects to the data source.");
    } catch (GenericEntityException e) {
        TransactionUtil.rollback(beganTransaction, "Error storing data from XPDL file", e);
        messages.add(e.getMessage());
        messages.add(e.toString());
        Debug.log(e);
    }
}

context.messages = messages;
