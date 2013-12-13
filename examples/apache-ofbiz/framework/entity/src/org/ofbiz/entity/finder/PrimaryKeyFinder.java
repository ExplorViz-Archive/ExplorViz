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
package org.ofbiz.entity.finder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.w3c.dom.Element;

/**
 * Uses the delegator to find entity values by a condition
 *
 */
@SuppressWarnings("serial")
public class PrimaryKeyFinder extends Finder {
    public static final String module = PrimaryKeyFinder.class.getName();

    protected FlexibleMapAccessor<Object> valueNameAcsr;
    protected FlexibleStringExpander autoFieldMapExdr;
    protected Map<FlexibleMapAccessor<Object>, Object> fieldMap;
    protected List<FlexibleStringExpander> selectFieldExpanderList;

    public PrimaryKeyFinder(Element entityOneElement) {
        super(entityOneElement);
        if (UtilValidate.isNotEmpty(entityOneElement.getAttribute("value-field"))) {
            this.valueNameAcsr = FlexibleMapAccessor.getInstance(entityOneElement.getAttribute("value-field"));
        } else {
            this.valueNameAcsr = FlexibleMapAccessor.getInstance(entityOneElement.getAttribute("value-name"));
        }
        this.autoFieldMapExdr = FlexibleStringExpander.getInstance(entityOneElement.getAttribute("auto-field-map"));

        // process field-map
        this.fieldMap = EntityFinderUtil.makeFieldMap(entityOneElement);

        // process select-field
        selectFieldExpanderList = EntityFinderUtil.makeSelectFieldExpanderList(entityOneElement);
    }

    @Override
    public void runFind(Map<String, Object> context, Delegator delegator) throws GeneralException {
        String entityName = this.entityNameExdr.expandString(context);

        String useCacheString = this.useCacheStrExdr.expandString(context);
        // default to false
        boolean useCacheBool = "true".equals(useCacheString);

        String autoFieldMapString = this.autoFieldMapExdr.expandString(context);
        // default to true
        boolean autoFieldMapBool = !"false".equals(autoFieldMapString);

        ModelEntity modelEntity = delegator.getModelEntity(entityName);
        if (modelEntity == null) {
            throw new IllegalArgumentException("No entity definition found for entity name [" + entityName + "]");
        }
        
        GenericValue valueOut = runFind(modelEntity, context, delegator, useCacheBool, autoFieldMapBool, this.fieldMap, this.selectFieldExpanderList);

        //Debug.logInfo("PrimaryKeyFinder: valueOut=" + valueOut, module);
        //Debug.logInfo("PrimaryKeyFinder: going into=" + this.valueNameAcsr.getOriginalName(), module);
        if (!valueNameAcsr.isEmpty()) {
           this.valueNameAcsr.put(context, valueOut);
        } else {
           if (valueOut != null) {
               context.putAll(valueOut);
           }
        }
    }

    public static GenericValue runFind(ModelEntity modelEntity, Map<String, Object> context, Delegator delegator, boolean useCache, boolean autoFieldMap,
            Map<FlexibleMapAccessor<Object>, Object> fieldMap, List<FlexibleStringExpander> selectFieldExpanderList) throws GeneralException {

        // assemble the field map
        Map<String, Object> entityContext = FastMap.newInstance();
        if (autoFieldMap) {
            // try a map called "parameters", try it first so values from here are overridden by values in the main context
            Object parametersObj = context.get("parameters");
            Boolean parametersObjExists = parametersObj != null && parametersObj instanceof Map<?, ?>;
            // only need PK fields
            Iterator<ModelField> iter = modelEntity.getPksIterator();
            while (iter.hasNext()) {
                ModelField curField = iter.next();
                String fieldName = curField.getName();
                Object fieldValue = null;
                if (parametersObjExists) {        
                    fieldValue = ((Map<?, ?>) parametersObj).get(fieldName);
                }
                if (context.containsKey(fieldName)) {
                    fieldValue = context.get(fieldName);
                }
                entityContext.put(fieldName, fieldValue);
            }
        }
        EntityFinderUtil.expandFieldMapToContext(fieldMap, context, entityContext);
        //Debug.logInfo("PrimaryKeyFinder: entityContext=" + entityContext, module);
        // then convert the types...
        
        // need the timeZone and locale for conversion, so add here and remove after
        entityContext.put("locale", context.get("locale"));
        entityContext.put("timeZone", context.get("timeZone"));
        modelEntity.convertFieldMapInPlace(entityContext, delegator);
        entityContext.remove("locale");
        entityContext.remove("timeZone");

        // get the list of fieldsToSelect from selectFieldExpanderList
        Set<String> fieldsToSelect = EntityFinderUtil.makeFieldsToSelect(selectFieldExpanderList, context);

        //if fieldsToSelect != null and useCacheBool is true, throw an error
        if (fieldsToSelect != null && useCache) {
            throw new IllegalArgumentException("Error in entity-one definition, cannot specify select-field elements when use-cache is set to true");
        }

        try {
            GenericValue valueOut = null;
            GenericPK entityPK = delegator.makePK(modelEntity.getEntityName(), entityContext);

            // make sure we have a full primary key, if any field is null then just log a warning and return null instead of blowing up
            if (entityPK.containsPrimaryKey(true)) {
                if (useCache) {
                    valueOut = delegator.findOne(entityPK.getEntityName(), entityPK, true);
                } else {
                    if (fieldsToSelect != null) {
                        valueOut = delegator.findByPrimaryKeyPartial(entityPK, fieldsToSelect);
                    } else {
                        valueOut = delegator.findOne(entityPK.getEntityName(), entityPK, false);
                    }
                }
            } else {
                if (Debug.infoOn()) Debug.logInfo("Returning null because found incomplete primary key in find: " + entityPK, module);
            }

            return valueOut;
        } catch (GenericEntityException e) {
            String errMsg = "Error finding entity value by primary key with entity-one: " + e.toString();
            Debug.logError(e, errMsg, module);
            throw new GeneralException(errMsg, e);
        }
    }
}
