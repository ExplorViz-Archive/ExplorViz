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
package org.ofbiz.webapp.event;

import java.util.*;

import javax.script.ScriptContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import javolution.util.FastMap;

import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.service.ExecutionServiceException;
import org.ofbiz.service.config.ServiceConfigUtil;
import org.ofbiz.webapp.control.ConfigXMLReader.Event;
import org.ofbiz.webapp.control.ConfigXMLReader.RequestMap;

public class GroovyEventHandler implements EventHandler {

    public static final String module = GroovyEventHandler.class.getName();
    protected static final Object[] EMPTY_ARGS = {};
    private static final Set<String> protectedKeys = createProtectedKeys();

    private static Set<String> createProtectedKeys() {
        Set<String> newSet = new HashSet<String>();
        newSet.add("request");
        newSet.add("response");
        newSet.add("session");
        newSet.add("dispatcher");
        newSet.add("delegator");
        newSet.add("security");
        newSet.add("locale");
        newSet.add("timeZone");
        newSet.add("userLogin");
        /* Commenting out for now because some scripts write to the parameters Map - which should not be allowed.
        newSet.add(ScriptUtil.PARAMETERS_KEY);
        */
        return Collections.unmodifiableSet(newSet);
    }

    private GroovyClassLoader groovyClassLoader;

    public void init(ServletContext context) throws EventHandlerException {
        try {
            // TODO: the name of the script base class is currently retrieved from the Groovy service engine configuration
            String scriptBaseClass = ServiceConfigUtil.getEngineParameter("groovy", "scriptBaseClass");
            if (scriptBaseClass != null) {
                CompilerConfiguration conf = new CompilerConfiguration();
                conf.setScriptBaseClass(scriptBaseClass);
                groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader(), conf);
            }
        } catch (GenericConfigException gce) {
            Debug.logWarning(gce, "Error retrieving the configuration for the groovy service engine: ", module);
        }
    }

    public String invoke(Event event, RequestMap requestMap, HttpServletRequest request, HttpServletResponse response) throws EventHandlerException {
        try {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("request", request);
            context.put("response", response);
            HttpSession session = request.getSession();
            context.put("session", session);
            context.put("dispatcher", request.getAttribute("dispatcher"));
            context.put("delegator", request.getAttribute("delegator"));
            context.put("security", request.getAttribute("security"));
            context.put("locale", UtilHttp.getLocale(request));
            context.put("timeZone", UtilHttp.getTimeZone(request));
            context.put("userLogin", session.getAttribute("userLogin"));
            context.put(ScriptUtil.PARAMETERS_KEY, UtilHttp.getCombinedMap(request, UtilMisc.toSet("delegator", "dispatcher", "security", "locale", "timeZone", "userLogin")));
            Object result = null;
            try {
                ScriptContext scriptContext = ScriptUtil.createScriptContext(context, protectedKeys);
                ScriptHelper scriptHelper = (ScriptHelper)scriptContext.getAttribute(ScriptUtil.SCRIPT_HELPER_KEY);
                if (scriptHelper != null) {
                    context.put(ScriptUtil.SCRIPT_HELPER_KEY, scriptHelper);
                }
                Script script = InvokerHelper.createScript(GroovyUtil.getScriptClassFromLocation(event.path, groovyClassLoader), GroovyUtil.getBinding(context));
                if (UtilValidate.isEmpty(event.invoke)) {
                    result = script.run();
                } else {
                    result = script.invokeMethod(event.invoke, EMPTY_ARGS);
                }
                if (result == null) {
                    result = scriptContext.getAttribute(ScriptUtil.RESULT_KEY);
                }
            } catch (Exception e) {
                Debug.logWarning(e, "Error running event " + event.path + ": ", module);
                request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
                return "error";
            }
            // check the result
            if (result instanceof Map) {
                Map resultMap = (Map)result;
                String successMessage = (String)resultMap.get("_event_message_");
                if (successMessage != null) {
                    request.setAttribute("_EVENT_MESSAGE_", successMessage);
                }
                String errorMessage = (String)resultMap.get("_error_message_");
                if (errorMessage != null) {
                    request.setAttribute("_ERROR_MESSAGE_", errorMessage);
                }
                return (String)resultMap.get("_response_code_");
            }
            if (result != null && !(result instanceof String)) {
                throw new EventHandlerException("Event did not return a String result, it returned a " + result.getClass().getName());
            }
            return (String) result;
        } catch (Exception e) {
            throw new EventHandlerException("Groovy Event Error", e);
        }
    }
}
