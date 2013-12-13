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
package org.ofbiz.entityext.eca;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.concurrent.ExecutionPool;
import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.config.MainResourceHandler;
import org.ofbiz.base.config.ResourceHandler;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.config.DelegatorInfo;
import org.ofbiz.entity.config.EntityConfigUtil;
import org.ofbiz.entity.config.EntityEcaReaderInfo;
import org.w3c.dom.Element;

/**
 * EntityEcaUtil
 */
public class EntityEcaUtil {

    public static final String module = EntityEcaUtil.class.getName();

    public static UtilCache<String, Map<String, Map<String, List<EntityEcaRule>>>> entityEcaReaders = UtilCache.createUtilCache("entity.EcaReaders", 0, 0, false);

    public static Map<String, Map<String, List<EntityEcaRule>>> getEntityEcaCache(String entityEcaReaderName) {
        Map<String, Map<String, List<EntityEcaRule>>> ecaCache = entityEcaReaders.get(entityEcaReaderName);
        if (ecaCache == null) {
            synchronized (EntityEcaUtil.class) {
                ecaCache = entityEcaReaders.get(entityEcaReaderName);
                if (ecaCache == null) {
                    ecaCache = FastMap.newInstance();
                    readConfig(entityEcaReaderName, ecaCache);
                    entityEcaReaders.put(entityEcaReaderName, ecaCache);
                }
            }
        }
        return ecaCache;
    }

    public static String getEntityEcaReaderName(String delegatorName) {
        DelegatorInfo delegatorInfo = EntityConfigUtil.getDelegatorInfo(delegatorName);
        if (delegatorInfo == null) {
            Debug.logError("BAD ERROR: Could not find delegator config with name: " + delegatorName, module);
            return null;
        }
        return delegatorInfo.entityEcaReader;
    }

    protected static void readConfig(String entityEcaReaderName, Map<String, Map<String, List<EntityEcaRule>>> ecaCache) {
        EntityEcaReaderInfo entityEcaReaderInfo = EntityConfigUtil.getEntityEcaReaderInfo(entityEcaReaderName);
        if (entityEcaReaderInfo == null) {
            Debug.logError("BAD ERROR: Could not find entity-eca-reader config with name: " + entityEcaReaderName, module);
            return;
        }

        List<Future<List<EntityEcaRule>>> futures = FastList.newInstance();
        for (Element eecaResourceElement: entityEcaReaderInfo.resourceElements) {
            ResourceHandler handler = new MainResourceHandler(EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME, eecaResourceElement);
            futures.add(ExecutionPool.GLOBAL_EXECUTOR.submit(createEcaLoaderCallable(handler)));
        }

        // get all of the component resource eca stuff, ie specified in each ofbiz-component.xml file
        for (ComponentConfig.EntityResourceInfo componentResourceInfo: ComponentConfig.getAllEntityResourceInfos("eca")) {
            if (entityEcaReaderName.equals(componentResourceInfo.readerName)) {
                futures.add(ExecutionPool.GLOBAL_EXECUTOR.submit(createEcaLoaderCallable(componentResourceInfo.createResourceHandler())));
            }
        }

        for (List<EntityEcaRule> oneFileRules: ExecutionPool.getAllFutures(futures)) {
            for (EntityEcaRule rule: oneFileRules) {
                String entityName = rule.getEntityName();
                String eventName = rule.getEventName();
                Map<String, List<EntityEcaRule>> eventMap = ecaCache.get(entityName);
                List<EntityEcaRule> rules = null;
                if (eventMap == null) {
                    eventMap = FastMap.newInstance();
                    rules = FastList.newInstance();
                    ecaCache.put(entityName, eventMap);
                    eventMap.put(eventName, rules);
                } else {
                    rules = eventMap.get(eventName);
                    if (rules == null) {
                        rules = FastList.newInstance();
                        eventMap.put(eventName, rules);
                    }
                }
                rules.add(rule);
            }
        }
    }

    private static List<EntityEcaRule> getEcaDefinitions(ResourceHandler handler) {
        List<EntityEcaRule> rules = FastList.newInstance();
        Element rootElement = null;
        try {
            rootElement = handler.getDocument().getDocumentElement();
        } catch (GenericConfigException e) {
            Debug.logError(e, module);
            return rules;
        }
        for (Element e: UtilXml.childElementList(rootElement, "eca")) {
            rules.add(new EntityEcaRule(e));
        }
        try {
            Debug.logImportant("Loaded [" + rules.size() + "] Entity ECA definitions from " + handler.getFullLocation() + " in loader " + handler.getLoaderName(), module);
        } catch (GenericConfigException e) {
            Debug.logError(e, module);
        }
        return rules;
    }

    protected static Callable<List<EntityEcaRule>> createEcaLoaderCallable(final ResourceHandler handler) {
        return new Callable<List<EntityEcaRule>>() {
            public List<EntityEcaRule> call() throws Exception {
                return getEcaDefinitions(handler);
            }
        };
    }

    public static Collection<EntityEcaRule> getEntityEcaRules(Delegator delegator, String entityName, String event) {
        Map<String, Map<String, List<EntityEcaRule>>> ecaCache = EntityEcaUtil.getEntityEcaCache(EntityEcaUtil.getEntityEcaReaderName(delegator.getDelegatorName()));
        Map<String, List<EntityEcaRule>> eventMap = ecaCache.get(entityName);
        if (eventMap != null) {
            if (event != null) {
                return eventMap.get(event);
            }
        }
        return null;
    }
}
