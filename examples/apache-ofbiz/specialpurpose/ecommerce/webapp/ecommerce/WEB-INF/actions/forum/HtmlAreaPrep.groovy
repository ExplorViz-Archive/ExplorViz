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

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.security.*;
import org.ofbiz.service.*;
import org.ofbiz.entity.model.*;
import org.ofbiz.widget.html.*;
import org.ofbiz.widget.form.*;
import org.ofbiz.content.data.DataResourceWorker;
import org.ofbiz.webapp.ftl.FreeMarkerViewHandler;

import java.io.StringWriter;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.WrappingTemplateModel;


import javax.servlet.*;
import javax.servlet.http.*;

contentIdTo = parameters.contentIdTo;

if (!contentIdTo || !contentIdTo.equals("TEMPLATE_MASTER")) {
    context.dynamicPrimaryHTMLField = "textData";
}
