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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.security.*;
import org.ofbiz.service.*;
import org.ofbiz.entity.model.*;
import org.ofbiz.widget.html.*;
import org.ofbiz.widget.form.*;
import org.ofbiz.content.data.DataResourceWorker;


import javax.servlet.*;
import javax.servlet.http.*;

formDefFile = page.formDefFile;
singleFormName = page.singleFormName;
//org.ofbiz.base.util.Debug.logInfo("in formprep, singleFormName:" + singleFormName, null);
entityName = page.entityName;
//defaultMapName = page.defaultMapName;
//if (!defaultMapName) defaultMapName = "currentValue";
defaultMapName = "currentValue";

singleWrapper = new HtmlFormWrapper(formDefFile, singleFormName, request, response);

// The idea here is that by setting the map name here, dependency on the
// widget-form config file could be eliminated.
modelForm = singleWrapper.getModelForm();
//modelForm.setDefaultMapName(defaultMapName);
currentValue = request.getAttribute("currentValue");
//org.ofbiz.base.util.Debug.logInfo("in formprep, currentValue:" + currentValue, null);
singleWrapper.putInContext(defaultMapName, currentValue);
context.singleWrapper = singleWrapper;
request.setAttribute("singleWrapper", singleWrapper);
