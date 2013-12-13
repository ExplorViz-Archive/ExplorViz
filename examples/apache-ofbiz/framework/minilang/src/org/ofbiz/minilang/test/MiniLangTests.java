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
package org.ofbiz.minilang.test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.service.testtools.OFBizTestCase;

public class MiniLangTests extends OFBizTestCase {

    private static final String module = MiniLangTests.class.getName();

    private final boolean traceEnabled;

    public MiniLangTests(String name) {
        super(name);
        traceEnabled = "true".equals(UtilProperties.getPropertyValue("minilang.properties", "unit.tests.trace.enabled"));
    }

    private Map<String, Object> createContext() {
        return UtilMisc.toMap("locale", Locale.US, "timeZone", TimeZone.getTimeZone("GMT"));
    }

    private MethodContext createServiceMethodContext() {
        MethodContext context = new MethodContext(dispatcher.getDispatchContext(), createContext(), null);
        context.setUserLogin(dispatcher.getDelegator().makeValidValue("UserLogin", UtilMisc.toMap("userLoginId", "system")), "userLogin");
        if (traceEnabled) {
            context.setTraceOn(Debug.INFO);
        }
        return context;
    }

    private SimpleMethod createSimpleMethod(String xmlString) throws Exception {
        return new SimpleMethod(UtilXml.readXmlDocument(xmlString).getDocumentElement(), module);
    }

    public void testAssignmentOperators() throws Exception {
        // <check-errors> and <add-error> tests
        SimpleMethod methodToTest = createSimpleMethod("<simple-method name=\"testCheckErrors\"><check-errors/></simple-method>");
        MethodContext context = createServiceMethodContext();
        String result = methodToTest.exec(context);
        assertEquals("<check-errors> success result", methodToTest.getDefaultSuccessCode(), result);
        List<String> messages = context.getEnv(methodToTest.getServiceErrorMessageListName());
        assertNull("<check-errors> null error message list", messages);
        methodToTest = createSimpleMethod("<simple-method name=\"testCheckErrors\"><add-error><fail-message message=\"This should fail\"/></add-error><check-errors/></simple-method>");
        context = createServiceMethodContext();
        result = methodToTest.exec(context);
        assertEquals("<check-errors> error result", methodToTest.getDefaultErrorCode(), result);
        messages = context.getEnv(methodToTest.getServiceErrorMessageListName());
        assertNotNull("<check-errors> error message list", messages);
        assertTrue("<check-errors> error message text", messages.contains("This should fail"));
        // <assert>, <not>,  and <if-empty> tests
        methodToTest = createSimpleMethod("<simple-method name=\"testAssert\"><assert><not><if-empty field=\"locale\"/></not></assert><check-errors/></simple-method>");
        context = createServiceMethodContext();
        result = methodToTest.exec(context);
        assertEquals("<assert> success result", methodToTest.getDefaultSuccessCode(), result);
        messages = context.getEnv(methodToTest.getServiceErrorMessageListName());
        assertNull("<assert> null error message list", messages);
        methodToTest = createSimpleMethod("<simple-method name=\"testAssert\"><assert><if-empty field=\"locale\"/></assert><check-errors/></simple-method>");
        context = createServiceMethodContext();
        result = methodToTest.exec(context);
        assertEquals("<assert> error result", methodToTest.getDefaultErrorCode(), result);
        messages = context.getEnv(methodToTest.getServiceErrorMessageListName());
        assertNotNull("<assert> error message list", messages);
        String errorMessage = messages.get(0);
        assertTrue("<assert> error message text", errorMessage.startsWith("Assertion failed:"));
    }

}
