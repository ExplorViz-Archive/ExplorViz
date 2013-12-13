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
package org.ofbiz.widget.tree;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.PatternSyntaxException;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.BshUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.finder.ByAndFinder;
import org.ofbiz.entity.finder.ByConditionFinder;
import org.ofbiz.entity.finder.EntityFinderUtil;
import org.ofbiz.entity.finder.PrimaryKeyFinder;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ModelService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Widget Library - Tree model class
 */
public abstract class ModelTreeAction {
    public static final String module = ModelTreeAction.class.getName();

    protected ModelTree modelTree;
    protected ModelTree.ModelNode modelNode;
    protected ModelTree.ModelNode.ModelSubNode modelSubNode;

    public ModelTreeAction(ModelTree.ModelNode modelNode, Element actionElement) {
        if (Debug.verboseOn()) Debug.logVerbose("Reading Tree action with name: " + actionElement.getNodeName(), module);
        this.modelNode = modelNode;
        this.modelTree = modelNode.getModelTree();
    }

    public ModelTreeAction(ModelTree.ModelNode.ModelSubNode modelSubNode, Element actionElement) {
        if (Debug.verboseOn()) Debug.logVerbose("Reading Tree action with name: " + actionElement.getNodeName(), module);
        this.modelSubNode = modelSubNode;
        this.modelNode = this.modelSubNode.getNode();
        this.modelTree = this.modelNode.getModelTree();
    }

    public abstract void runAction(Map<String, Object> context);

/*
    public static List readSubActions(ModelTree.ModelNode modelNode, Element parentElement) {
        List actions = new LinkedList();

        List actionElementList = UtilXml.childElementList(parentElement);
        Iterator actionElementIter = actionElementList.iterator();
        while (actionElementIter.hasNext()) {
            Element actionElement = (Element) actionElementIter.next();
            if ("set".equals(actionElement.getNodeName())) {
                actions.add(new SetField(modelTree, actionElement));
            } else if ("script".equals(actionElement.getNodeName())) {
                actions.add(new Script(modelTree, actionElement));
            } else if ("service".equals(actionElement.getNodeName())) {
                actions.add(new Service(modelTree, actionElement));
            } else if ("entity-one".equals(actionElement.getNodeName())) {
                actions.add(new EntityOne(modelTree, actionElement));
            } else if ("entity-and".equals(actionElement.getNodeName())) {
                actions.add(new EntityAnd(modelTree, actionElement));
            } else if ("entity-condition".equals(actionElement.getNodeName())) {
                actions.add(new EntityCondition(modelTree, actionElement));
            } else {
                throw new IllegalArgumentException("Action element not supported with name: " + actionElement.getNodeName());
            }
        }

        return actions;
    }
    */

    public static void runSubActions(List<? extends ModelTreeAction> actions, Map<String, Object> context) {
        for (ModelTreeAction action: actions) {
            if (Debug.verboseOn()) Debug.logVerbose("Running tree action " + action.getClass().getName(), module);
            action.runAction(context);
        }
    }

    public static class SetField extends ModelTreeAction {
        protected FlexibleMapAccessor<Object> field;
        protected FlexibleMapAccessor<Object> fromField;
        protected FlexibleStringExpander valueExdr;
        protected FlexibleStringExpander globalExdr;
        protected String type;

        public SetField(ModelTree.ModelNode modelNode, Element setElement) {
            super (modelNode, setElement);
            this.field = FlexibleMapAccessor.getInstance(setElement.getAttribute("field"));
            this.fromField = FlexibleMapAccessor.getInstance(setElement.getAttribute("from-field"));
            this.valueExdr = FlexibleStringExpander.getInstance(setElement.getAttribute("value"));
            this.globalExdr = FlexibleStringExpander.getInstance(setElement.getAttribute("global"));
            this.type = setElement.getAttribute("type");
            if (!this.fromField.isEmpty() && !this.valueExdr.isEmpty()) {
                throw new IllegalArgumentException("Cannot specify a from-field [" + setElement.getAttribute("from-field") + "] and a value [" + setElement.getAttribute("value") + "] on the set action in a tree widget");
            }
        }

        @Override
        public void runAction(Map<String, Object> context) {
            String globalStr = this.globalExdr.expandString(context);
            // default to false
            boolean global = "true".equals(globalStr);

            Object newValue = null;
            if (!this.fromField.isEmpty()) {
                newValue = this.fromField.get(context);
            } else if (!this.valueExdr.isEmpty()) {
                newValue = this.valueExdr.expandString(context);
            }
            if (UtilValidate.isNotEmpty(this.type)) {
                if ("NewMap".equals(this.type)) {
                    newValue = FastMap.newInstance();
                } else if ("NewList".equals(this.type)) {
                    newValue = FastList.newInstance();
                } else {
                    try {
                        newValue = ObjectType.simpleTypeConvert(newValue, this.type, null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
                    } catch (GeneralException e) {
                        String errMsg = "Could not convert field value for the field: [" + this.field.getOriginalName() + "] to the [" + this.type + "] type for the value [" + newValue + "]: " + e.toString();
                        Debug.logError(e, errMsg, module);
                        throw new IllegalArgumentException(errMsg);
                    }
                }
            }
            this.field.put(context, newValue);

            if (global) {
                Map<String, Object> globalCtx = UtilGenerics.checkMap(context.get("globalContext"));
                if (globalCtx != null) {
                    this.field.put(globalCtx, newValue);
                }
            }

            // this is a hack for backward compatibility with the JPublish page object
            Map<String, Object> page = UtilGenerics.checkMap(context.get("page"));
            if (page != null) {
                this.field.put(page, newValue);
            }
        }
    }

    public static class Script extends ModelTreeAction {
        protected String location;

        public Script(ModelTree.ModelNode modelNode, Element scriptElement) {
            super (modelNode, scriptElement);
            this.location = scriptElement.getAttribute("location");
        }

        public Script(ModelTree.ModelNode.ModelSubNode modelSubNode, Element scriptElement) {
            super (modelSubNode, scriptElement);
            this.location = scriptElement.getAttribute("location");
        }

        @Override
        public void runAction(Map<String, Object> context) {
            if (location.endsWith(".bsh")) {
                try {
                    context.put("_LIST_ITERATOR_", null);
                    BshUtil.runBshAtLocation(location, context);
                    Object obj = context.get("_LIST_ITERATOR_");
                    if (this.modelSubNode != null) {
                        if (obj != null && (obj instanceof EntityListIterator || obj instanceof ListIterator<?>)) {
                            ListIterator<? extends Map<String, ? extends Object>> listIt = UtilGenerics.cast(obj);
                            this.modelSubNode.setListIterator(listIt);
                        } else {
                            if (obj instanceof List<?>) {
                                List<? extends Map<String, ? extends Object>> list = UtilGenerics.checkList(obj);
                                this.modelSubNode.setListIterator(list.listIterator());
                            }
                        }
                    }
                } catch (GeneralException e) {
                    String errMsg = "Error running BSH script at location [" + location + "]: " + e.toString();
                    Debug.logError(e, errMsg, module);
                    throw new IllegalArgumentException(errMsg);
                }
            } else {
                throw new IllegalArgumentException("For tree script actions the script type is not yet support for location:" + location);
            }
        }
    }

    public static class Service extends ModelTreeAction {
        protected FlexibleStringExpander serviceNameExdr;
        protected FlexibleMapAccessor<Map<String, Object>> resultMapNameAcsr;
        protected FlexibleStringExpander autoFieldMapExdr;
        protected FlexibleStringExpander resultMapListNameExdr;
        protected FlexibleStringExpander resultMapValueNameExdr;
        protected FlexibleStringExpander valueNameExdr;
        protected Map<FlexibleMapAccessor<Object>, Object> fieldMap;

        public Service(ModelTree.ModelNode modelNode, Element serviceElement) {
            super (modelNode, serviceElement);
            initService(serviceElement);
        }

        public Service(ModelTree.ModelNode.ModelSubNode modelSubNode, Element serviceElement) {
            super (modelSubNode, serviceElement);
            initService(serviceElement);
        }

        public void initService(Element serviceElement) {

            this.serviceNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("service-name"));
            this.resultMapNameAcsr = FlexibleMapAccessor.getInstance(serviceElement.getAttribute("result-map"));
            if (this.resultMapNameAcsr.isEmpty()) this.resultMapNameAcsr = FlexibleMapAccessor.getInstance(serviceElement.getAttribute("result-map-name"));
            this.autoFieldMapExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("auto-field-map"));
            this.resultMapListNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("result-map-list"));
            if (this.resultMapListNameExdr.isEmpty()) this.resultMapListNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("result-map-list-name"));
            if (this.resultMapListNameExdr.isEmpty()) this.resultMapListNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("result-map-list-iterator-name"));
            this.resultMapValueNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("result-map-value"));
            if (this.resultMapValueNameExdr.isEmpty()) this.resultMapValueNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("result-map-value-name"));
            this.valueNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("value"));
            if (this.valueNameExdr.isEmpty()) this.valueNameExdr = FlexibleStringExpander.getInstance(serviceElement.getAttribute("value-name"));
            this.fieldMap = EntityFinderUtil.makeFieldMap(serviceElement);
        }

        @Override
        public void runAction(Map<String, Object> context) {
            String serviceNameExpanded = this.serviceNameExdr.expandString(context);
            if (UtilValidate.isEmpty(serviceNameExpanded)) {
                throw new IllegalArgumentException("Service name was empty, expanded from: " + this.serviceNameExdr.getOriginal());
            }

            String autoFieldMapString = this.autoFieldMapExdr.expandString(context);
            boolean autoFieldMapBool = !"false".equals(autoFieldMapString);

            try {
                Map<String, Object> serviceContext = null;
                if (autoFieldMapBool) {
                    serviceContext = this.modelTree.getDispatcher().getDispatchContext().makeValidContext(serviceNameExpanded, ModelService.IN_PARAM, context);
                } else {
                    serviceContext = FastMap.newInstance();
                }

                if (this.fieldMap != null) {
                    EntityFinderUtil.expandFieldMapToContext(this.fieldMap, context, serviceContext);
                }

                Map<String, Object> result = this.modelTree.getDispatcher().runSync(serviceNameExpanded, serviceContext);

                if (!this.resultMapNameAcsr.isEmpty()) {
                    this.resultMapNameAcsr.put(context, result);
                    String queryString = (String)result.get("queryString");
                    context.put("queryString", queryString);
                    context.put("queryStringMap", result.get("queryStringMap"));
                    if (UtilValidate.isNotEmpty(queryString)) {
                        try {
                            String queryStringEncoded = queryString.replaceAll("&", "%26");
                            context.put("queryStringEncoded", queryStringEncoded);
                        } catch (PatternSyntaxException e) {

                        }
                    }
                } else {
                    context.putAll(result);
                }
                String resultMapListName = resultMapListNameExdr.expandString(context);
                //String resultMapListIteratorName = resultMapListIteratorNameExdr.expandString(context);
                String resultMapValueName = resultMapValueNameExdr.expandString(context);
                String valueName = valueNameExdr.expandString(context);

                if (this.modelSubNode != null) {
                    //ListIterator iter = null;
                    if (UtilValidate.isNotEmpty(resultMapListName)) {
                        List<? extends Map<String, ? extends Object>> lst = UtilGenerics.checkList(result.get(resultMapListName));
                        if (lst != null) {
                            if (lst instanceof ListIterator<?>) {
                                ListIterator<? extends Map<String, ? extends Object>> listIt = UtilGenerics.cast(lst);
                                this.modelSubNode.setListIterator(listIt);
                            } else {
                                this.modelSubNode.setListIterator(lst.listIterator());
                            }
                        }
                    }
                } else {
                    if (UtilValidate.isNotEmpty(resultMapValueName)) {
                        if (UtilValidate.isNotEmpty(valueName)) {
                            context.put(valueName, result.get(resultMapValueName));
                        } else {
                            Map<String, Object> resultMap = UtilGenerics.checkMap(result.get(resultMapValueName));
                            context.putAll(resultMap);
                        }
                    }
                }
            } catch (GenericServiceException e) {
                String errMsg = "Error calling service with name " + serviceNameExpanded + ": " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
    }

    public static class EntityOne extends ModelTreeAction {
        protected PrimaryKeyFinder finder;
        String valueName;

        public EntityOne(ModelTree.ModelNode modelNode, Element entityOneElement) {
            super (modelNode, entityOneElement);

            this.valueName = UtilFormatOut.checkEmpty(entityOneElement.getAttribute("value"), entityOneElement.getAttribute("value-name"));
            if (UtilValidate.isEmpty(this.valueName)) this.valueName = null;
            entityOneElement.setAttribute("value", this.valueName);

            finder = new PrimaryKeyFinder(entityOneElement);
        }

        @Override
        public void runAction(Map<String, Object> context) {
            try {
                finder.runFind(context, this.modelTree.getDelegator());
            } catch (GeneralException e) {
                String errMsg = "Error doing entity query by condition: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
    }

    public static class EntityAnd extends ModelTreeAction {
        protected ByAndFinder finder;
        String listName;

        public EntityAnd(ModelTree.ModelNode.ModelSubNode modelSubNode, Element entityAndElement) {
            super (modelSubNode, entityAndElement);
            boolean useCache = "true".equalsIgnoreCase(entityAndElement.getAttribute("use-cache"));
            Document ownerDoc = entityAndElement.getOwnerDocument();
            if (!useCache) UtilXml.addChildElement(entityAndElement, "use-iterator", ownerDoc);

            this.listName = UtilFormatOut.checkEmpty(entityAndElement.getAttribute("list"), entityAndElement.getAttribute("list-name"));
            if (UtilValidate.isEmpty(this.listName)) this.listName = "_LIST_ITERATOR_";
            entityAndElement.setAttribute("list-name", this.listName);

            finder = new ByAndFinder(entityAndElement);
        }

        @Override
        public void runAction(Map<String, Object> context) {
            try {
                context.put(this.listName, null);
                finder.runFind(context, this.modelTree.getDelegator());
                Object obj = context.get(this.listName);
                if (obj != null && (obj instanceof EntityListIterator || obj instanceof ListIterator<?>)) {
                    ListIterator<? extends Map<String, ? extends Object>> listIt = UtilGenerics.cast(obj);
                    this.modelSubNode.setListIterator(listIt);
                } else {
                    if (obj instanceof List<?>) {
                        List<? extends Map<String, ? extends Object>> list = UtilGenerics.checkList(obj);
                        this.modelSubNode.setListIterator(list.listIterator());
                    }
                }
            } catch (GeneralException e) {
                String errMsg = "Error doing entity query by condition: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
    }

    public static class EntityCondition extends ModelTreeAction {
        ByConditionFinder finder;
        String listName;

        public EntityCondition(ModelTree.ModelNode.ModelSubNode modelSubNode, Element entityConditionElement) {
            super (modelSubNode, entityConditionElement);
            Document ownerDoc = entityConditionElement.getOwnerDocument();
            boolean useCache = "true".equalsIgnoreCase(entityConditionElement.getAttribute("use-cache"));
            if (!useCache) UtilXml.addChildElement(entityConditionElement, "use-iterator", ownerDoc);

            this.listName = UtilFormatOut.checkEmpty(entityConditionElement.getAttribute("list"), entityConditionElement.getAttribute("list-name"));
            if (UtilValidate.isEmpty(this.listName)) this.listName = "_LIST_ITERATOR_";
            entityConditionElement.setAttribute("list-name", this.listName);

            finder = new ByConditionFinder(entityConditionElement);
        }

        @Override
        public void runAction(Map<String, Object> context) {
            try {
                context.put(this.listName, null);
                finder.runFind(context, this.modelTree.getDelegator());
                Object obj = context.get(this.listName);
                if (obj != null && (obj instanceof EntityListIterator || obj instanceof ListIterator<?>)) {
                    ListIterator<? extends Map<String, ? extends Object>> listIt = UtilGenerics.cast(obj);
                    this.modelSubNode.setListIterator(listIt);
                } else {
                    if (obj instanceof List<?>) {
                        List<? extends Map<String, ? extends Object>> list = UtilGenerics.cast(obj);
                        this.modelSubNode.setListIterator(list.listIterator());
                    }
                }
            } catch (GeneralException e) {
                String errMsg = "Error doing entity query by condition: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
    }
}
